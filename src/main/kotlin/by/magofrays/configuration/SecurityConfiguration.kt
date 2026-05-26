package by.magofrays.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.rsocket.EnableRSocketSecurity
import org.springframework.security.config.annotation.rsocket.RSocketSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtReactiveAuthenticationManager
import org.springframework.security.rsocket.core.PayloadSocketAcceptorInterceptor
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import java.security.interfaces.RSAPublicKey


@Configuration
@EnableWebFluxSecurity
@EnableRSocketSecurity
@EnableReactiveMethodSecurity
class SecurityConfiguration {
    @Value("\${spring.security.jwt.public-key}")
    lateinit var publicKey: RSAPublicKey

    @Bean
    fun reactiveJwtDecoder(): ReactiveJwtDecoder {
        return NimbusReactiveJwtDecoder.withPublicKey(publicKey)
            .signatureAlgorithm(SignatureAlgorithm.RS256).build()
    }

    @Bean
    fun securityFilterChain(security: ServerHttpSecurity): SecurityWebFilterChain {
        return security
            .cors { cors -> cors.configurationSource(corsConfigurationSource()) }
            .csrf { csrf -> csrf.disable() }
            .authorizeExchange { exchange -> exchange.anyExchange().authenticated() }
            .oauth2ResourceServer { oauth2 -> oauth2.jwt { jwt -> jwt.jwtDecoder(reactiveJwtDecoder()) } }
            .build()
    }

    @Bean
    fun authorizationRSocket(security: RSocketSecurity): PayloadSocketAcceptorInterceptor {
        return security
            .authorizePayload { authorize ->
                authorize
                    .anyRequest().authenticated()
                    .anyExchange().permitAll()
            }
            .jwt { jwtSpec ->
                jwtSpec.authenticationManager(
                    JwtReactiveAuthenticationManager(reactiveJwtDecoder())
                )
            }
            .build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val cors = CorsConfiguration()
        cors.allowedOrigins = listOf("*")
        cors.allowedMethods = listOf("*")
        cors.allowCredentials = true
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", cors)
        return source
    }


}