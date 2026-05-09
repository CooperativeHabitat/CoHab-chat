package by.magofrays.service

import by.magofrays.dto.ClientMessage
import by.magofrays.dto.MessageDto
import by.magofrays.entity.Message
import by.magofrays.repository.MessageRepository
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import java.util.*

@Service
class MessageService(
    val messageRepository: MessageRepository,
    val redisTemplate: ReactiveRedisTemplate<String, MessageDto>,
) {
    private val log = LoggerFactory.getLogger(MessageService::class.java)

    fun connect(familyId: UUID, messages: Flux<ClientMessage>): Flux<MessageDto> {
        val channel = "family:chat:$familyId"
        val subscription = redisTemplate
            .listenTo(ChannelTopic.of(channel))
            .map { it.message }
            .replay(1)
            .autoConnect(0)
        log.info("Created $channel")

        messages
            .flatMap { message ->
                val entity = Message(
                    content = message.content,
                    replyToId = message.replyToId.toString(),
                    familyId = familyId.toString(),
                    memberId = message.memberId.toString(),
                )

                messageRepository.save(entity)
                    .map { saved ->
                        MessageDto(
                            content = saved.content,
                            replyToId = UUID.fromString(saved.replyToId),
                            familyId = UUID.fromString(saved.familyId),
                            memberId = UUID.fromString(saved.memberId),
                            sentAt = saved.sentAt,
                            updatedAt = saved.updatedAt
                        )
                    }.doOnError { error -> log.error("Error occurred while saving message: ${error.message}") }
                    .flatMap { dto ->
                        redisTemplate.convertAndSend(channel, dto)
                            .doOnSuccess {
                                log.info("Sent message to redis: $dto")
                            }
                            .thenReturn(dto)
                    }
            }
            .doOnError { error ->
                println("Error while handling messages from client: ${error.message}")
            }
            .subscribe()
        return subscription
    }


}