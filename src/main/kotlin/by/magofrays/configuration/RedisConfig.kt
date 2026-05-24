package by.magofrays.configuration

import by.magofrays.dto.ChatResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig {

    @Bean
    fun reactiveRedisTemplate(
        redisConnectionFactory: ReactiveRedisConnectionFactory
    ): ReactiveRedisTemplate<String, ChatResponse> {

        val serializer = JacksonJsonRedisSerializer(ChatResponse::class.java)

        val context = RedisSerializationContext
            .newSerializationContext<String, ChatResponse>(StringRedisSerializer())
            .value(serializer)
            .build()

        return ReactiveRedisTemplate(redisConnectionFactory, context)
    }
}