package by.magofrays.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("spring.security.cors")
class CorsProperties {
    var allowedOrigins: List<String> = emptyList()
    var allowedMethods: List<String> = emptyList()
    var allowedHeaders: List<String> = emptyList()
    var allowCredentials: Boolean = false
    var maxAge: Long = 3600L
}

