//package by.magofrays.configuration
//
//import org.springframework.beans.factory.annotation.Value
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.Configuration
//import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
//import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity
//import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
//import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
//import org.springframework.web.cors.CorsConfiguration
//import org.springframework.web.cors.reactive.CorsConfigurationSource
//import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
//import java.security.interfaces.RSAPublicKey
//
//
//@Configuration
//@EnableWebFluxSecurity
//class SecurityConfiguration{
//    @Value($$"${spring.security.jwt.public-key}")
//    lateinit var publicKey: RSAPublicKey
//
//    @Bean
//    fun reactiveJwtDecoder(): ReactiveJwtDecoder {
//        return NimbusReactiveJwtDecoder.withPublicKey(publicKey).build()
//    }
//
//
//    @Bean
//    fun corsConfigurationSource(): CorsConfigurationSource {
//        val cors = CorsConfiguration()
//        cors.allowedOrigins = listOf("*")
//        cors.allowedMethods = listOf("*")
//        cors.allowCredentials = true
//        val source = UrlBasedCorsConfigurationSource()
//        source.apply { registerCorsConfiguration("/**", cors) }
//        return source
//    }
//
//
//}