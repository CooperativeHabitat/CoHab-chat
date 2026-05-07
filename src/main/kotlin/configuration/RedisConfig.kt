package by.magofrays.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import by.magofrays.dto.MessageDto

@Configuration
class RedisConfig {

    @Bean
    fun reactiveRedisTemplate(
        redisConnectionFactory: ReactiveRedisConnectionFactory
    ): ReactiveRedisTemplate<String, MessageDto> {

        val serializer = JacksonJsonRedisSerializer(MessageDto::class.java)

        val context = RedisSerializationContext
            .newSerializationContext<String, MessageDto>(StringRedisSerializer())
            .value(serializer)
            .build()

        return ReactiveRedisTemplate(redisConnectionFactory, context)
    }
}