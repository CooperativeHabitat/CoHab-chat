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

    suspend fun getFuckingToken(): Jwt {
        return ReactiveSecurityContextHolder.getContext()
            .map { it.authentication?.principal as Jwt }
            .awaitSingle()
    }

//    @PreAuthorize("hasAuthority('USER')")
    @MessageMapping("chat.{familyId}.stream")
    suspend fun connectFamilyChat(
        @DestinationVariable familyId: String
    ): Flux<ChatResponse> {
        val memberId = UUID.fromString(getFuckingToken().subject)
        return familyMessageService.connectFamilyChatStream(memberId, familyId)
    }

//    @PreAuthorize("hasAuthority('USER') && hasPermission(#request.familyId, 'family', 'CREATE_MESSAGE')")
    @MessageMapping("chat.send")
    suspend fun sendNewMessage(
        @Payload request: CreateMessageRequest
    ) {
        val memberId = UUID.fromString(getFuckingToken().subject)
        familyMessageService.createMessage(memberId, request)
    }

//    @PreAuthorize("hasAuthority('USER') && hasPermission(#request.familyId, 'family', 'EDIT_MESSAGE')")
    @MessageMapping("chat.edit")
    suspend fun editMessage(
        @Payload request: EditMessageRequest
    ) {
        val memberId = UUID.fromString(getFuckingToken().subject)
        familyMessageService.editMessage(memberId, request)
    }

//    @PreAuthorize("hasAuthority('USER') && hasPermission(#request.familyId, 'family', 'SHOW_MESSAGE')")
    @MessageMapping("chat.view")
    suspend fun viewMessage(
        @Payload request: ViewMessageRequest
    ) {
        val memberId = UUID.fromString(getFuckingToken().subject)
        familyMessageService.viewMessage(memberId, request)
    }

    @PreAuthorize("hasAuthority('USER') && hasPermission(#request.familyId, 'family', 'REACT_MESSAGE')")
    @MessageMapping("chat.react")
    suspend fun reactMessage(
        @Payload request: ReactMessageRequest
    ) {
        val memberId = UUID.fromString(getFuckingToken().subject)
        familyMessageService.reactMessage(memberId, request)
    }

//    @PreAuthorize("hasAuthority('USER') && hasPermission(#request.familyId, 'family', 'DELETE_MESSAGE')")
    @MessageMapping("chat.delete")
    suspend fun deleteMessage(
        @Payload request: DeleteMessageRequest
    ) {
        val memberId = UUID.fromString(getFuckingToken().subject)
        familyMessageService.deleteMessage(memberId, request)
    }

//    @PreAuthorize("hasAuthority('USER') && hasPermission(#familyId, 'family', 'SHOW_MESSAGE')")
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