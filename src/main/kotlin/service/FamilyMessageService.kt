package by.magofrays.service

import by.magofrays.dto.ClientMessage
import by.magofrays.dto.MessageDto
import by.magofrays.entity.Message
import by.magofrays.repository.MessageRepository
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers
import java.util.*

@Service
class FamilyMessageService(
    val messageRepository: MessageRepository,
    val redisTemplate: ReactiveRedisTemplate<String, MessageDto>
) {

    fun connect(familyId: UUID, messages: Flux<ClientMessage>): Flux<MessageDto> {
        val channel = "family:chat:$familyId"
        val subscription = redisTemplate
            .listenTo(ChannelTopic.of(channel))
            .map { it.message }
            .replay(1)
            .autoConnect(0)
        println("subscribed on redis")

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
                    }
                    .flatMap { dto ->
                        redisTemplate.convertAndSend(channel, dto)
                            .doOnSuccess {
                                println("Sent to redis")
                            }
                            .thenReturn(dto)
                    }
            }
            .doOnError { error ->
                println("ERROR: ${error.message}")
            }
            .subscribe()
        return subscription
    }


}