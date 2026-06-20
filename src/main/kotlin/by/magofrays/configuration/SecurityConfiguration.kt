package by.magofrays.configuration

import by.magofrays.dto.client.CreateMessageRequest
import by.magofrays.dto.client.DeleteMessageRequest
import by.magofrays.dto.client.EditMessageRequest
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.annotation.rsocket.EnableRSocketSecurity
import org.springframework.security.config.annotation.rsocket.RSocketSecurity
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration.jwtDecoder
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtReactiveAuthenticationManager
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter
import org.springframework.security.rsocket.core.PayloadSocketAcceptorInterceptor
import org.springframework.security.rsocket.util.matcher.PayloadExchangeAuthorizationContext
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import reactor.core.publisher.Mono
import java.io.Serializable
import java.security.interfaces.RSAPublicKey
import java.util.*


@Configuration
@EnableRSocketSecurity
class SecurityConfiguration(
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, List<String>>,
    private val corsProperties: CorsProperties
) {
    private val log = LoggerFactory.getLogger(SecurityConfiguration::class.java)

    @Value("\${spring.security.jwt.public-key}")
    lateinit var rsaPublicKey: RSAPublicKey

    @Bean
    fun reactiveJwtDecoder(): ReactiveJwtDecoder {
        return NimbusReactiveJwtDecoder.withPublicKey(rsaPublicKey)
            .signatureAlgorithm(SignatureAlgorithm.RS256).build()
    }

    @Bean
    fun jwtReactiveAuthenticationManager(jwtDecoder: ReactiveJwtDecoder): JwtReactiveAuthenticationManager {
        return JwtReactiveAuthenticationManager(jwtDecoder)
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val cors = CorsConfiguration()
        cors.allowedOrigins = corsProperties.allowedOrigins
        cors.allowedMethods = corsProperties.allowedMethods
        cors.allowedHeaders = corsProperties.allowedMethods
        cors.allowCredentials = corsProperties.allowCredentials
        cors.maxAge = corsProperties.maxAge
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", cors)
        return source
    }

    @Bean
    fun authorizationRSocket(security: RSocketSecurity): PayloadSocketAcceptorInterceptor {
        return security
            .authorizePayload { authz ->
                authz
                    .setup().permitAll()
                    .route("api.notification.notifications")
                    .hasAuthority("SCOPE_USER")
                    .route("api.notification.stream")
                    .hasAuthority("SCOPE_USER")
                    .route("api.family.chat.{familyId}.stream")
                    .access { authentication, context ->
                        val familyId = context.variables["familyId"] as String
                        mono { hasPermission(authentication, UUID.fromString(familyId), "SHOW_CHAT") }
                    }
                    .route("api.family.chat.send")
                    .access { authentication, context ->
                        val request = parsePayload<CreateMessageRequest>(context)
                        mono { hasPermission(authentication, request.familyId, "CREATE_MESSAGE") }
                    }

                    .route("api.family.chat.edit")
                    .access { authentication, context ->
                        val request = parsePayload<EditMessageRequest>(context)
                        mono { hasPermission(authentication, request.familyId, "EDIT_MESSAGE") }
                    }

                    .route("api.family.chat.delete")
                    .access { authentication, context ->
                        val request = parsePayload<DeleteMessageRequest>(context)
                        mono { hasPermission(authentication, request.familyId, "DELETE_MESSAGE") }
                    }

                    .route("api.family.messages.{familyId}")
                    .access { authentication, context ->
                        val familyId = context.variables["familyId"] as String
                        mono { hasPermission(authentication, UUID.fromString(familyId), "SHOW_CHAT") }
                    }

                    .anyRequest().authenticated()
                    .anyExchange().permitAll()
            }
            .jwt(withDefaults())
            .build()
    }

    private inline fun <reified T> parsePayload(context: PayloadExchangeAuthorizationContext): T {
        val data = String(context.exchange.payload.dataUtf8.toByteArray())
        return jacksonObjectMapper().readValue(data, T::class.java)
    }

    suspend fun hasPermission(
        monoAuth: Mono<Authentication>,
        targetId: Serializable,
        permission: String
    ): AuthorizationDecision {
        val auth = monoAuth.awaitSingle()
        if (!auth.isAuthenticated) {
            return AuthorizationDecision(false)
        }
        val jwt = auth.principal as? Jwt ?: return AuthorizationDecision(false)
        val memberId = UUID.fromString(jwt.subject)
        val familyId = targetId as UUID
        val key = "family:accesses::$familyId:$memberId"
        try {
            val accesses = getAccessesFromCache(key)
            return AuthorizationDecision(accesses.any { it == permission } && jwt.getClaim<List<String>>("scope")?.firstOrNull().equals( C"USER"))
        } catch (ex: Exception) {
            log.info("Exception occurred during checking accesses: {}", ex.message)
            return AuthorizationDecision(false)
        }

    }

    private suspend fun getAccessesFromCache(key: String): List<String> {
        return reactiveRedisTemplate.opsForValue().get(key)
            .onErrorReturn(emptyList()).awaitSingle()
    }
}