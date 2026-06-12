package by.magofrays.configuration
import by.magofrays.service.KafkaSenderService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.json.JacksonJsonDecoder
import org.springframework.http.codec.json.JacksonJsonEncoder
import org.springframework.messaging.rsocket.RSocketStrategies
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler
import org.springframework.util.MimeTypeUtils
import org.springframework.web.util.pattern.PathPatternRouteMatcher
import tools.jackson.databind.ObjectMapper


@Configuration
class RSocketConfiguration() {

    private val log = LoggerFactory.getLogger(this::class.java)

    @Bean
    fun rSocketStrategies(objectMapper: ObjectMapper): RSocketStrategies {
        return RSocketStrategies.builder()
            .encoder(JacksonJsonEncoder())
            .decoder(JacksonJsonDecoder())
            .routeMatcher(PathPatternRouteMatcher())
            .metadataExtractorRegistry { registry ->
                registry.metadataToExtract(
                    MimeTypeUtils.APPLICATION_JSON,
                    Map::class.java
                ) { metadataString, map ->
                    map["route"] = metadataString["route"]!!
                }
            }
            .build()
    }
    @Bean
    fun rsocketMessageHandler(strategies: RSocketStrategies) = RSocketMessageHandler().apply {
            rSocketStrategies = strategies
            routeMatcher = PathPatternRouteMatcher()
        }
}