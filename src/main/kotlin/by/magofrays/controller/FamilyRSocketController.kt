package by.magofrays.controller

import by.magofrays.dto.ChatResponse
import by.magofrays.dto.MessageDto
import by.magofrays.dto.MessageRequest
import by.magofrays.dto.client.CreateMessageRequest
import by.magofrays.dto.client.DeleteMessageRequest
import by.magofrays.dto.client.EditMessageRequest
import by.magofrays.dto.client.ReactMessageRequest
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
import java.util.UUID

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

    @MessageMapping("chat.send")
    fun sendNewMessage(
        @Payload request: CreateMessageRequest) : Mono<Void> {
        val memberId = UUID.randomUUID()
        return familyMessageService.createMessage(memberId, request).then()
    }

    @MessageMapping("chat.edit")
    fun editMessage(
        @Payload request: EditMessageRequest) : Mono<Void> {
        return familyMessageService.editMessage(UUID.randomUUID(), request)
    }

    @MessageMapping("chat.view")
    fun viewMessage(
        @Payload request: ViewMessageRequest) : Mono<Void> {
        return familyMessageService.viewMessage(UUID.randomUUID(), request)
    }

    @MessageMapping("chat.react")
    fun reactMessage(
        @Payload request: ReactMessageRequest) : Mono<Void> {
        return familyMessageService.reactMessage(UUID.randomUUID(), request)
    }

    @MessageMapping("chat.delete")
    fun deleteMessage(
        @Payload request: DeleteMessageRequest) : Mono<Void> {
        return familyMessageService.deleteMessage(UUID.randomUUID(), request)
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