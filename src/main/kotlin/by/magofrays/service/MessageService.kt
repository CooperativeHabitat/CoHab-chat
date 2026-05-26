package by.magofrays.service

import by.magofrays.dto.ChatResponse
import by.magofrays.dto.MessageDto
import by.magofrays.dto.client.*
import by.magofrays.entity.MessageRead
import by.magofrays.entity.Reaction
import by.magofrays.repository.MessageRepository
import by.magofrays.mapper.MessageMapper
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

    fun createMessage(memberId: UUID, request: CreateMessageRequest): Mono<Void> {
        val channel = "family:chat:${request.familyId}"
        val messageEntity = messageMapper.toEntity(request)
        messageEntity.memberId = memberId.toString()

        return messageRepository.save(messageEntity)
            .map { message ->
                messageMapper.toChatResponse(message).apply {
                    operationType = ChatResponse.ChatOperationType.NEW_MESSAGE
                }
            }
            .flatMap { chatResponse ->
                log.info("Sending new message {} to family {}", chatResponse, request.familyId)
                chatChannel.convertAndSend(channel, chatResponse)
            }
            .then()
    }

    fun editMessage(memberId: UUID, request: EditMessageRequest): Mono<Void> {
        val channel = "family:chat:${request.familyId}"

        return messageRepository.findById(request.messageId)
            .flatMap { messageEntity ->
                messageEntity.content = request.content
                messageEntity.updatedAt = Instant.now()
                messageRepository.save(messageEntity)
                    .map { messageMapper.toChatResponse(it).apply {
                        operationType = ChatResponse.ChatOperationType.EDIT_MESSAGE
                    }}
            }
            .flatMap { chatResponse ->
                log.info("Sending edited message {} to family {}", chatResponse, request.familyId)
                chatChannel.convertAndSend(channel, chatResponse)
            }
            .then()
    }

    fun deleteMessage(memberId: UUID, request: DeleteMessageRequest): Mono<Void> {
        val channel = "family:chat:${request.familyId}"

        return messageRepository.findById(request.messageId)
            .flatMap { messageEntity ->
                val chatResponse = messageMapper.toChatResponse(messageEntity).apply {
                    operationType = ChatResponse.ChatOperationType.DELETE_MESSAGE
                }
                log.info("Sending deleted message {} to family {}", chatResponse, request.familyId)
                messageRepository.delete(messageEntity)
                    .then(chatChannel.convertAndSend(channel, chatResponse))
            }
            .then()
    }

    fun viewMessage(memberId: UUID, request: ViewMessageRequest): Mono<Void> {
        val channel = "family:chat:${request.familyId}"

        return messageRepository.findById(request.messageId)
            .flatMap { messageEntity ->
                val currentReads = messageEntity.reads ?: emptyList()
                if (currentReads.none { it.memberId == memberId.toString() }) {
                    messageEntity.reads = currentReads + MessageRead(
                        memberId = memberId.toString()
                    )
                }
                messageRepository.save(messageEntity)
                    .map { messageMapper.toChatResponse(it).apply {
                        operationType = ChatResponse.ChatOperationType.VIEWED_MESSAGE
                    }}
            }
            .flatMap { chatResponse ->
                chatChannel.convertAndSend(channel, chatResponse)
            }
            .then()
    }

    fun reactMessage(memberId: UUID, request: ReactMessageRequest): Mono<Void> {
        val channel = "family:chat:${request.familyId}"

        return messageRepository.findById(request.messageId)
            .flatMap { messageEntity ->
                val currentReactions = messageEntity.reactions ?: emptyList()
                messageEntity.reactions = currentReactions
                    .filter { it.memberId != memberId.toString() } +
                        Reaction(memberId = memberId.toString(), reaction = request.reaction)

                messageRepository.save(messageEntity)
                    .map { messageMapper.toChatResponse(it).apply {
                        operationType = ChatResponse.ChatOperationType.REACTION_ON_MESSAGE
                    }}
            }
            .flatMap { chatResponse ->
                chatChannel.convertAndSend(channel, chatResponse)
            }
            .then()
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