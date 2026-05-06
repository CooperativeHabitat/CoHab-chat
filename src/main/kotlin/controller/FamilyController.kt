package by.magofrays.controller

import by.magofrays.dto.MessageDto
import by.magofrays.service.FamilyMessageService
import kotlinx.coroutines.flow.Flow
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux

@Controller
@MessageMapping("api.family")
class FamilyController(val familyMessageService: FamilyMessageService) {

    @MessageMapping("chat.{familyId}")
    suspend fun stream(@DestinationVariable familyId: String) : Flux<MessageDto> = familyMessageService.liveStream(familyId)

    @MessageMapping("chat")
    suspend fun postStream(
        @Payload inboundMessages : Flow<MessageDto>) = familyMessageService.post(inboundMessages)

    @MessageMapping("chat.from.{familyId}.{messageId}")
    suspend fun start(){}

}