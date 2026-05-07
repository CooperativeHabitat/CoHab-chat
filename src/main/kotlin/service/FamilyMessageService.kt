package by.magofrays.service

import by.magofrays.dto.ClientMessage
import by.magofrays.dto.MessageDto
import by.magofrays.entity.Message
import by.magofrays.repository.MessageRepository
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
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
            .share()

        messages.doOnNext { message ->
            val messageEntity = Message(
                content = message.content,
                replyToId = message.replyToId,
                familyId = familyId,
                memberId = message.memberId,
            )
            messageRepository.save(messageEntity)
                .map { entity ->
                    val messageDto = MessageDto(
                        content = entity.content,
                        replyToId = entity.replyToId,
                        familyId = entity.familyId,
                        memberId = entity.memberId,
                        sentAt = messageEntity.sentAt,
                        updatedAt = messageEntity.updatedAt
                    )
                    redisTemplate.convertAndSend(channel, messageDto)
                        .thenReturn(messageDto)
                }.subscribe()
        }.subscribe()

        return subscription
    }
}