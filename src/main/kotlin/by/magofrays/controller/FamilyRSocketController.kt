package by.magofrays.controller

import by.magofrays.dto.ClientMessage
import by.magofrays.dto.MessageDto
import by.magofrays.service.MessageService
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux
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


}