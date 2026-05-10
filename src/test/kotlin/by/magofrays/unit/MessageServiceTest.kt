package by.magofrays.unit

import by.magofrays.dto.ClientMessage
import by.magofrays.dto.MessageDto
import by.magofrays.entity.Message
import by.magofrays.repository.MessageRepository
import by.magofrays.service.MessageService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.redis.connection.ReactiveSubscription
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.junit.jupiter.SpringExtension
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import reactor.test.StepVerifier
import tools.jackson.databind.ObjectMapper
import java.time.Instant
import java.util.*

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [MessageService::class, ObjectMapper::class])
class MessageServiceTest {
    @Autowired
    private lateinit var messageService: MessageService

    @MockitoBean
    private lateinit var messageRepository: MessageRepository

    @MockitoBean
    private lateinit var redisTemplate: ReactiveRedisTemplate<String, MessageDto>

    @Test
    fun `should send and receive own messages after connection`() {
        val familyId = UUID.randomUUID()
        val memberId = UUID.randomUUID()
        val clientMessage = ClientMessage(
            memberId = memberId,
            content = "content",
            replyToId = null
        )

        val channel = "family:chat:$familyId"
        val redisSink = Sinks.many().multicast().onBackpressureBuffer<ReactiveSubscription.Message<String, MessageDto>>()

        Mockito.`when`(redisTemplate.listenTo(ChannelTopic.of(channel)))
            .thenReturn(redisSink.asFlux())

        Mockito.`when`(redisTemplate.convertAndSend(Mockito.eq(channel), any(MessageDto::class.java)))
            .thenAnswer { invocation ->
                val dto = invocation.arguments[1] as MessageDto
                val message = object : ReactiveSubscription.Message<String, MessageDto> {
                    override fun getChannel(): String = channel
                    override fun getMessage(): MessageDto = dto
                }
                redisSink.tryEmitNext(message)
                Mono.just(1L)
            }

        Mockito.`when`(messageRepository.save(any(Message::class.java)))
            .thenAnswer { invocation ->
                val messageToSave = invocation.arguments[0] as Message
                messageToSave.id = UUID.randomUUID().toString()
                Mono.just(messageToSave)
            }

        val clientSink = Sinks.many().unicast().onBackpressureBuffer<ClientMessage>()
        val chatFlux = messageService.connectMessages(familyId, clientSink.asFlux())

        StepVerifier.create(chatFlux)
            .then { clientSink.tryEmitNext(clientMessage) }
            .expectNextMatches { dto ->
                dto.content == clientMessage.content &&
                        dto.memberId == clientMessage.memberId.toString() &&
                        dto.familyId == familyId.toString()
            }
            .then {clientSink.tryEmitNext(clientMessage) }
            .expectNextMatches { dto ->
                dto.content == clientMessage.content &&
                        dto.memberId == clientMessage.memberId.toString() &&
                        dto.familyId == familyId.toString() }
            .thenCancel()
            .verify()
    }

    @Test
    fun `should receive messages from other client`(){
        val familyId = UUID.randomUUID()
        val otherClientId = UUID.randomUUID()
        val clientMessageDto = MessageDto(
            memberId = otherClientId.toString(),
            content = "content from other client",
            sentAt = Instant.now(),
            updatedAt = Instant.now(),
            familyId = familyId.toString()
        )
        val channel = "family:chat:$familyId"
        val redisSink = Sinks.many().multicast().onBackpressureBuffer<ReactiveSubscription.Message<String, MessageDto>>()
        Mockito.`when`(redisTemplate.listenTo(ChannelTopic.of(channel)))
            .thenReturn(redisSink.asFlux())
        val chatFlux = messageService.connectMessages(familyId, Flux.empty())
        StepVerifier.create(chatFlux)
            .then { redisSink.tryEmitNext(
                object : ReactiveSubscription.Message<String, MessageDto> {
                    override fun getChannel(): String = channel
                    override fun getMessage(): MessageDto = clientMessageDto
                }
            ) }
            .expectNext(clientMessageDto)
            .thenCancel()
            .verify()
    }

    @Test
    fun `should cancel subscription for client messages after client cancel subscription for chat`() {
        val familyId = UUID.randomUUID()
        val channel = "family:chat:$familyId"
        val clientSink = Sinks.many().unicast().onBackpressureBuffer<ClientMessage>()
        val redisSink = Sinks.many().multicast().onBackpressureBuffer<ReactiveSubscription.Message<String, MessageDto>>()

        Mockito.`when`(redisTemplate.listenTo(ChannelTopic.of(channel)))
            .thenReturn(redisSink.asFlux())
        val chatFlux = messageService.connectMessages(familyId, clientSink.asFlux())

        StepVerifier.create(chatFlux)
            .thenCancel()
            .verify()

        val testMessage = ClientMessage(
            memberId = UUID.randomUUID(),
            content = "test",
            replyToId = null
        )
        clientSink.tryEmitNext(testMessage)
        Mockito.verify(messageRepository, Mockito.never()).save(any())
        Mockito.verify(redisTemplate, Mockito.never()).convertAndSend(any(), any())
    }


    @Test
    fun `should return page of messages`(){
        val familyId = UUID.randomUUID()
        val pageable = PageRequest.of(0, 3)
        val startDate = Instant.now().minusSeconds(3600)
        val endDate = Instant.now()
        val messages = (1..3).map { index ->
            Message(
                memberId = UUID.randomUUID().toString(),
                content = "content from some client",
                sentAt = Instant.now(),
                updatedAt = Instant.now(),
                familyId = familyId.toString()
            )
        }

        val notificationDtos = messages.map { message ->
            MessageDto(
                memberId = message.memberId,
                content = message.content,
                sentAt = message.sentAt,
                updatedAt = message.updatedAt,
                familyId = message.familyId
            )
        }
        Mockito.`when`(messageRepository.findByFamilyIdAndSentAtBetween(familyId.toString(), startDate, endDate, pageable))
            .thenReturn(Flux.fromIterable(messages))
        Mockito.`when`(messageRepository.countByFamilyIdAndSentAtBetween(familyId.toString(), startDate, endDate))
            .thenReturn(Mono.just(3L))
        val expected = PageImpl(notificationDtos, pageable, 3L)
        val monoResult = messageService.findAllMessagesByFamily(familyId.toString(), startDate, endDate, pageable)
        val verifier = StepVerifier.create(monoResult)
            .expectNext(expected)
        verifier.verifyComplete()
    }


}