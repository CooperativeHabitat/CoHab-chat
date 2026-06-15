package by.magofrays.service

import by.magofrays.dto.ChatResponse
import by.magofrays.dto.MessageDto
import by.magofrays.dto.client.*
import by.magofrays.entity.MessageRead
import by.magofrays.entity.Reaction
import by.magofrays.mapper.MessageMapper
import by.magofrays.repository.mongo.MessageRepository
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import java.time.Instant
import java.util.*

@Service
class MessageService(
    val messageRepository: MessageRepository,
    val chatChannel: ReactiveRedisTemplate<String, ChatResponse>,
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

    suspend fun createMessage(memberId: UUID, request: CreateMessageRequest) {
        val channel = "family:chat:${request.familyId}"
        val messageEntity = messageMapper.toEntity(request)
        messageEntity.memberId = memberId.toString()
        val savedMessage = messageRepository.save(messageEntity).awaitSingle()
        val chatResponse = messageMapper.toChatResponse(savedMessage)
        chatResponse.operationType = ChatResponse.ChatOperationType.NEW_MESSAGE
        log.info("Sending new message {} to family {}", chatResponse, request.familyId)
        chatChannel.convertAndSend(channel, chatResponse).awaitSingle()
    }

    suspend fun editMessage(memberId: UUID, request: EditMessageRequest) {
        val channel = "family:chat:${request.familyId}"
        val messageEntity = messageRepository.findById(request.messageId).awaitSingle() // todo check null
        messageEntity.content = request.content
        messageEntity.updatedAt = Instant.now()
        messageRepository.save(messageEntity).awaitSingle()
        val chatResponse = messageMapper.toChatResponse(messageEntity)
        chatResponse.operationType = ChatResponse.ChatOperationType.EDIT_MESSAGE
        log.info("Sending edited message {} to family {}", chatResponse, request.familyId)
        chatChannel.convertAndSend(channel, chatResponse).awaitSingle()
    }

    suspend fun deleteMessage(memberId: UUID, request: DeleteMessageRequest) {
        val channel = "family:chat:${request.familyId}"
        val messageEntity = messageRepository.findById(request.messageId).awaitSingle()
        val chatResponse = messageMapper.toChatResponse(messageEntity)
        chatResponse.operationType = ChatResponse.ChatOperationType.DELETE_MESSAGE
        log.info("Sending deleted message {} to family {}", chatResponse, request.familyId)
        messageRepository.delete(messageEntity).awaitSingleOrNull()
        chatChannel.convertAndSend(channel, chatResponse).awaitSingle()
    }

    suspend fun viewMessage(memberId: UUID, request: ViewMessageRequest) {
        val channel = "family:chat:${request.familyId}"
        val messageEntity = messageRepository.findById(request.messageId).awaitSingle()
        val currentReads = messageEntity.reads ?: emptyList()
        if (currentReads.none { it.memberId == memberId.toString() }) {
            messageEntity.reads = currentReads + MessageRead(
                memberId = memberId.toString()
            )
        }
        messageRepository.save(messageEntity).awaitSingle()
        val chatResponse = messageMapper.toChatResponse(messageEntity)
        chatResponse.operationType = ChatResponse.ChatOperationType.VIEWED_MESSAGE
        chatChannel.convertAndSend(channel, chatResponse).awaitSingle()
    }

    suspend fun reactMessage(memberId: UUID, request: ReactMessageRequest) {
        val channel = "family:chat:${request.familyId}"
        val messageEntity = messageRepository.findById(request.messageId).awaitSingle()
        val currentReactions = messageEntity.reactions ?: emptyList()
        val reaction = Reaction(memberId.toString(), request.reaction)
        messageEntity.reactions = currentReactions.filter{ it.memberId != memberId.toString() } + reaction
        messageRepository.save(messageEntity).awaitSingle()
        val chatResponse = messageMapper.toChatResponse(messageEntity)
        chatResponse.operationType = ChatResponse.ChatOperationType.REACTION_ON_MESSAGE
        chatChannel.convertAndSend(channel, chatResponse).awaitSingle()
    }

    suspend fun findAllMessagesByFamily(
        familyId: String,
        startDate: Instant,
        endDate: Instant,
        pageable: Pageable
    ): List<MessageDto> {
        val messages = messageRepository.findByFamilyIdAndSentAtBetweenOrderBySentAtDesc(familyId, startDate, endDate, pageable)
            .collectList().awaitSingle()
        val messageDtos = messages.map { messageMapper.toDto(it) }
        return messageDtos
    }

}