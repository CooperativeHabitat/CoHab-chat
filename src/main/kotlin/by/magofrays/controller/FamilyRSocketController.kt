package by.magofrays.controller

import by.magofrays.dto.ChatResponse
import by.magofrays.dto.MessageDto
import by.magofrays.dto.MessageRequest
import by.magofrays.dto.client.*
import by.magofrays.service.MessageService
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.domain.PageRequest
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux
import java.util.*

@Controller
@MessageMapping("api.family")
class FamilyRSocketController(
    val familyMessageService: MessageService,
) {

    suspend fun getMemberToken(): Jwt {
        return ReactiveSecurityContextHolder.getContext()
            .map { it.authentication?.principal as Jwt }
            .awaitSingle()
    }


    @MessageMapping("chat.{familyId}.stream")
    suspend fun connectFamilyChat(
        @DestinationVariable familyId: String
    ): Flux<ChatResponse> {
        return familyMessageService.connectFamilyChatStream(familyId)
    }


    @MessageMapping("chat.send")
    suspend fun sendNewMessage(
        @Payload request: CreateMessageRequest

    ) {
        val memberId = UUID.fromString(getMemberToken().subject)
        familyMessageService.createMessage(memberId, request)
    }


    @MessageMapping("chat.edit")
    suspend fun editMessage(
        @Payload request: EditMessageRequest
    ) {
        val memberId = UUID.fromString(getMemberToken().subject)
        familyMessageService.editMessage(memberId, request)
    }

    @MessageMapping("chat.view")
    suspend fun viewMessage(
        @Payload request: ViewMessageRequest
    ) {
        val memberId = UUID.fromString(getMemberToken().subject)
        familyMessageService.viewMessage(memberId, request)
    }


    @MessageMapping("chat.react")
    suspend fun reactMessage(
        @Payload request: ReactMessageRequest
    ) {
        val memberId = UUID.fromString(getMemberToken().subject)
        familyMessageService.reactMessage(memberId, request)
    }

    @MessageMapping("chat.delete")
    suspend fun deleteMessage(
        @AuthenticationPrincipal token: Jwt,
        @Payload request: DeleteMessageRequest
    ) {
        val memberId = UUID.fromString(getMemberToken().subject)
        return familyMessageService.deleteMessage(memberId, request)
    }

    @MessageMapping("messages.{familyId}")
    suspend fun findAllMessagesByFamily(
        @DestinationVariable familyId: UUID,
        @Payload request: MessageRequest
    ): List<MessageDto> {
        val pageable = PageRequest.of(request.page, request.size)
        return familyMessageService.findAllMessagesByFamily(
                familyId.toString(),
                request.startDate,
                request.endDate,
                pageable
            )
    }
}