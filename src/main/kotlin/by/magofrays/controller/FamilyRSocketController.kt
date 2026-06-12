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
import kotlinx.coroutines.reactor.mono
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Controller
@MessageMapping("api.family")
class FamilyRSocketController(
    val familyMessageService: MessageService,
) {

//    @PreAuthorize("hasAuthority('USER') && hasPermission(#familyId, 'family', 'SHOW_CHAT')")
    @MessageMapping("chat.{familyId}.stream")
    fun connectFamilyChat(
//        @AuthenticationPrincipal memberToken: Jwt,
        @DestinationVariable familyId: String) : Flux<ChatResponse> {
//        val memberId = UUID.fromString(memberToken.subject)
        return familyMessageService.connectFamilyChatStream(UUID.randomUUID(), familyId)
    }

//    @PreAuthorize("hasAuthority('USER') && hasPermission(#request.familyId, 'family', 'CREATE_MESSAGE')")
    @MessageMapping("chat.send")
    fun sendNewMessage(
//        @AuthenticationPrincipal memberToken: Jwt,
        @Payload request: CreateMessageRequest) : Mono<Void> {
        val memberId = UUID.randomUUID()
        return mono {
            familyMessageService.createMessage(memberId, request)
        }.then()
    }

//    @PreAuthorize("hasAuthority('USER') && hasPermission(#request.familyId, 'family', 'EDIT_MESSAGE')")
    @MessageMapping("chat.edit")
    fun editMessage(
        @AuthenticationPrincipal memberToken: Jwt,
        @Payload request: EditMessageRequest) : Mono<Void> {
        val memberId = UUID.fromString(memberToken.subject)
        return mono {
            familyMessageService.editMessage(memberId, request) }
            .then()
    }

//    @PreAuthorize("hasAuthority('USER') && hasPermission(#request.familyId, 'family', 'SHOW_MESSAGE')")
    @MessageMapping("chat.view")
    fun viewMessage(
        @AuthenticationPrincipal memberToken: Jwt,
        @Payload request: ViewMessageRequest) : Mono<Void> {
        val memberId = UUID.fromString(memberToken.subject)
        return mono { familyMessageService.viewMessage(memberId, request) }.then()
    }

//    @PreAuthorize("hasAuthority('USER') && hasPermission(#request.familyId, 'family', 'REACT_MESSAGE')")
    @MessageMapping("chat.react")
    fun reactMessage(
        @AuthenticationPrincipal memberToken: Jwt,
        @Payload request: ReactMessageRequest) : Mono<Void> {
        val memberId = UUID.fromString(memberToken.subject)
        return mono { familyMessageService.reactMessage(memberId, request) }.then()
    }

//    @PreAuthorize("hasAuthority('USER') && hasPermission(#request.familyId, 'family', 'DELETE_MESSAGE')")
    @MessageMapping("chat.delete")
    fun deleteMessage(
        @AuthenticationPrincipal memberToken: Jwt,
        @Payload request: DeleteMessageRequest) : Mono<Void> {
        val memberId = UUID.fromString(memberToken.subject)
        return mono{ familyMessageService.deleteMessage(memberId, request) }.then()
    }


//    @PreAuthorize("hasAuthority('USER') && hasPermission(#request.familyId, 'family', 'SHOW_MESSAGE')")
    @MessageMapping("messages.{familyId}")
    fun findAllMessagesByFamily(
//        @AuthenticationPrincipal memberToken: Jwt,
        @DestinationVariable familyId: UUID,
        @Payload request: MessageRequest
    ): Mono<List<MessageDto>> {
        val pageable = PageRequest.of(request.page, request.size)
        val result = mono {familyMessageService.findAllMessagesByFamily(
            familyId.toString(),
            request.startDate,
            request.endDate,
            pageable
        )
        }
        return result
    }
}