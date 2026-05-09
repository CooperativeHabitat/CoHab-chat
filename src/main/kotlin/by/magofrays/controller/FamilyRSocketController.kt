package by.magofrays.controller

import by.magofrays.dto.ClientMessage
import by.magofrays.dto.MessageDto
import by.magofrays.dto.MessageRequest
import by.magofrays.service.MessageService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@Controller
@MessageMapping("api.family")
class FamilyRSocketController(
    val familyMessageService: MessageService,
) {

    @MessageMapping("chat.{familyId}")
    fun connect(@DestinationVariable familyId: UUID,
                @Payload clientMessages: Flux<ClientMessage>) : Flux<MessageDto> {
        return familyMessageService.connect(familyId, clientMessages)
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