package by.magofrays.service

import by.magofrays.dto.MessageDto
import by.magofrays.entity.Message
import by.magofrays.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import tools.jackson.databind.ObjectMapper
import java.util.*

@Service
class FamilyMessageService(
    val messageRepository: MessageRepository,
    val objectMapper: ObjectMapper
) {

    fun liveStream(familyId: String): Flux<MessageDto> {
        return messageRepository.findByFamilyId(UUID.fromString(familyId))
            .map { entity ->
                objectMapper.convertValue(entity, MessageDto::class.java)
            }
    }

    suspend fun post(messageFlow: Flow<MessageDto>) {
        messageFlow
            .map { dto -> objectMapper.convertValue(dto, Message::class.java) }
            .collect { message ->
                messageRepository.save(message).awaitSingle()
            }
    }
}