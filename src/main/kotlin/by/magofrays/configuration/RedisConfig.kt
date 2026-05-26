package by.magofrays.configuration

import by.magofrays.dto.ChatResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import tools.jackson.databind.ObjectMapper

@Configuration
class RedisConfig {

    @Bean
    fun chatRedisTemplate(
        redisConnectionFactory: ReactiveRedisConnectionFactory
    ): ReactiveRedisTemplate<String, ChatResponse> {

        val serializer = JacksonJsonRedisSerializer(ChatResponse::class.java)

        val context = RedisSerializationContext
            .newSerializationContext<String, ChatResponse>(StringRedisSerializer())
            .value(serializer)
            .build()

        return ReactiveRedisTemplate(redisConnectionFactory, context)
    }

    @Bean
    fun accessesRedisTemplate(
        redisConnectionFactory: ReactiveRedisConnectionFactory,
        objectMapper: ObjectMapper
    ): ReactiveRedisTemplate<String, List<String>> {


        val serializer = GenericJacksonJsonRedisSerializer(objectMapper) as RedisSerializer<List<String>>


        val context = RedisSerializationContext
            .newSerializationContext<String, List<String>>(StringRedisSerializer())
            .value(serializer)
            .build()

        return ReactiveRedisTemplate(redisConnectionFactory, context)
    }
}