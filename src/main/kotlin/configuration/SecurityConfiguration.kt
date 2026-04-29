package by.magofrays.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import java.security.interfaces.RSAPublicKey


@Configuration
class SecurityConfiguration {
    @Value($$"${spring.security.jwt.public-key}")
    lateinit var publicKey: RSAPublicKey

    @Bean
    fun reactiveJwtDecoder(): ReactiveJwtDecoder {
        return NimbusReactiveJwtDecoder.withPublicKey(publicKey).build()
    }


    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val cors = CorsConfiguration()
        cors.allowedOrigins = listOf("*")
        cors.allowedMethods = listOf("*")
        cors.allowCredentials = true
        val source = UrlBasedCorsConfigurationSource()
        source.apply { registerCorsConfiguration("/**", cors) }
        return source
    }

    @Bean
    fun securityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .authorizeExchange { exchanges ->
                exchanges
                    .pathMatchers("/api/public/**", "/actuator/health").permitAll()
                    .anyExchange().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.jwtDecoder(reactiveJwtDecoder())
                }
            }
            .build()
    }
}