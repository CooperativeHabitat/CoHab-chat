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
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.annotation.rsocket.EnableRSocketSecurity
import org.springframework.security.config.annotation.rsocket.RSocketSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.rsocket.core.PayloadSocketAcceptorInterceptor
import org.springframework.security.rsocket.util.matcher.PayloadExchangeAuthorizationContext
import reactor.core.publisher.Mono
import java.io.Serializable
import java.security.interfaces.RSAPublicKey
import java.util.*


@Configuration
@EnableRSocketSecurity
class RSocketSecurityConfiguration(
    val reactiveRedisTemplate: ReactiveRedisTemplate<String, List<String>>,
) {
    private val log = LoggerFactory.getLogger(RSocketSecurityConfiguration::class.java)

    @Value("\${spring.security.jwt.public-key}")
    lateinit var publicKey: RSAPublicKey

    @Bean
    fun authorizationRSocket(security: RSocketSecurity): PayloadSocketAcceptorInterceptor {
        return security
            .authorizePayload { authz ->
                authz
                    .setup().permitAll()
                    .route("api.notification.notifications")
                    .permitAll()
//                    .hasAuthority("USER")
                    .route("api.notification.stream")
                    .permitAll()
//                    .hasAuthority("USER")
                    .route("api.family.chat.{familyId}.stream")
                    .access { authentication, context ->
                        val familyId = context.variables["familyId"] as String
                        mono{hasPermission(authentication, UUID.fromString(familyId), "SHOW_CHAT")}
                    }
                    .route("api.family.chat.send")
                    .access { authentication, context ->
                        val request = parsePayload<CreateMessageRequest>(context)
                        mono{hasPermission(authentication, request.familyId, "CREATE_MESSAGE")}
                    }

                    .route("api.family.chat.edit")
                    .access { authentication, context ->
                        val request = parsePayload<EditMessageRequest>(context)
                        mono{hasPermission(authentication, request.familyId, "EDIT_MESSAGE")}
                    }

                    .route("api.family.chat.delete")
                    .access { authentication, context ->
                        val request = parsePayload<DeleteMessageRequest>(context)
                        mono{hasPermission(authentication, request.familyId, "DELETE_MESSAGE")}
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
            return AuthorizationDecision(accesses.any { it == permission })
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