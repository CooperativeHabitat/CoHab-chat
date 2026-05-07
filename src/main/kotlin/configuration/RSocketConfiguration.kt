package by.magofrays.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.http.codec.json.JacksonJsonDecoder
import org.springframework.http.codec.json.JacksonJsonEncoder
import org.springframework.messaging.rsocket.RSocketStrategies
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler
import org.springframework.web.util.pattern.PathPatternRouteMatcher


@Configuration
class RSocketConfiguration() {

    @Bean
    fun rSocketStrategies(): RSocketStrategies {
        return RSocketStrategies.builder()
            .encoder(JacksonJsonEncoder())
            .decoder(JacksonJsonDecoder())
            .routeMatcher(PathPatternRouteMatcher())
            .metadataExtractorRegistry { registry ->
                registry.metadataToExtract(MediaType.APPLICATION_JSON, String::class.java, "route")
            }
            .build()
    }
    @Bean
    fun rsocketMessageHandler(strategies: RSocketStrategies) = RSocketMessageHandler().apply {
            rSocketStrategies = strategies
            routeMatcher = PathPatternRouteMatcher()
        }
}