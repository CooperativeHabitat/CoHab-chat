package by.magofrays.controller

import by.magofrays.dto.ChatResponse
import by.magofrays.dto.MessageDto
import by.magofrays.dto.MessageRequest
import by.magofrays.dto.client.CreateMessageRequest
import by.magofrays.dto.client.EditMessageRequest
import by.magofrays.dto.client.ViewMessageRequest
import by.magofrays.service.MessageService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Controller
@MessageMapping("api.family")
class FamilyRSocketController(
    val familyMessageService: MessageService,
) {

    @MessageMapping("chat.{familyId}.stream")
    fun connectFamilyChat(
        @DestinationVariable familyId: String) : Flux<ChatResponse> {
        return familyMessageService.connectFamilyChatStream(familyId)
    }

    @MessageMapping("chat.{familyId}.send")
    fun sendNewMessage(
        @DestinationVariable familyId: String,
        @Payload request: CreateMessageRequest) : Mono<ChatResponse>? {
        return null
    }

    @MessageMapping("chat.{familyId}.edit")
    fun editMessage(
        @DestinationVariable familyId: String,
        @Payload request: EditMessageRequest) : Mono<ChatResponse>? {
        return null
    }

    @MessageMapping("chat.{familyId}.view")
    fun viewMessage(
        @DestinationVariable familyId: String,
        @Payload request: ViewMessageRequest) : Mono<ChatResponse>? {
        return null
    }

    @MessageMapping("chat.{familyId}.react")
    fun reactMessage(
        @DestinationVariable familyId: String,
        @Payload request: MessageDto) : Mono<ChatResponse>? {
        return null
    }

    @MessageMapping("chat.{familyId}.delete")
    fun deleteMessage(
        @DestinationVariable familyId: String,
        @Payload request: MessageDto) : Mono<ChatResponse>? {
        return null
    }


    @MessageMapping("messages.{familyId}")
    fun findAllMessagesByFamily(
        @DestinationVariable familyId: String,
        @Payload request: MessageRequest
    ): Mono<Page<MessageDto>> {
        val pageable = PageRequest.of(request.page, request.size)
        return familyMessageService.findAllMessagesByFamily(
            familyId,
            request.startDate,
            request.endDate,
            pageable
        )
    }
}