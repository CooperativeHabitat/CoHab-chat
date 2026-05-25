package by.magofrays.service

import by.magofrays.dto.ChatResponse
import by.magofrays.dto.MessageDto
import by.magofrays.dto.client.CreateMessageRequest
import by.magofrays.dto.client.EditMessageRequest
import by.magofrays.repository.MessageRepository
import mapper.MessageMapper
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import tools.jackson.databind.ObjectMapper
import java.time.Instant
import java.util.*

@Service
class MessageService(
    val messageRepository: MessageRepository,
    val chatChannel: ReactiveRedisTemplate<String, ChatResponse>,
    val objectMapper: ObjectMapper,
    val messageMapper: MessageMapper
) {
    private val log = LoggerFactory.getLogger(MessageService::class.java)


    fun connectFamilyChatStream(familyId: String): Flux<ChatResponse> {
        val channel = "family:chat:$familyId"
        val subscription = chatChannel
            .listenTo(ChannelTopic.of(channel))
            .map { it.message }
            .replay(1)
            .autoConnect(0)
        log.info("Created $channel")
        return subscription
            .doOnNext { log.debug("Sent client event") }
            .doOnCancel { log.info("Canceled subscription to client") }
    }

    fun createMessage(memberId: UUID, createMessageRequest: CreateMessageRequest): Mono<ChatResponse>? {
        val channel = "family:chat:${createMessageRequest.familyId}"
        val messageEntity = messageMapper.toEntity(createMessageRequest)
        messageEntity.memberId = memberId.toString()
        return messageRepository.save(messageEntity).map {
            message -> messageMapper.toChatResponse(message)
        }.doOnNext {
            chatResponse ->
            log.info("Sending message to family {} with message {}", createMessageRequest.familyId, chatResponse)
            chatChannel.convertAndSend(channel, chatResponse)
        }

    }

    fun editMessage(familyId: UUID, messageId: UUID, request: EditMessageRequest): Mono<ChatResponse>? {
        return null
    }

    fun deleteMessage(familyId: UUID, messageId: UUID): Mono<ChatResponse>? {
        return null
    }

    fun findAllMessagesByFamily(
        familyId: String,
        startDate: Instant?,
        endDate: Instant?,
        pageable: Pageable
    ): Mono<Page<MessageDto>> {
        return messageRepository.findByFamilyIdAndSentAtBetween(familyId, startDate, endDate, pageable)
            .map { message -> objectMapper.convertValue(message, MessageDto::class.java) }
            .collectList()
            .zipWith(messageRepository.countByFamilyIdAndSentAtBetween(familyId, startDate, endDate))
            .map { p -> PageImpl(p.getT1(), pageable, p.getT2()) }
    }


}