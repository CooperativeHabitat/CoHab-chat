package by.magofrays.mapper

import by.magofrays.dto.ChatResponse
import by.magofrays.dto.MessageDto
import by.magofrays.dto.client.CreateMessageRequest
import by.magofrays.entity.Message
import org.mapstruct.AfterMapping
import org.mapstruct.Mapper
import org.mapstruct.MappingTarget
import java.time.Instant

@Mapper(componentModel = "spring")
abstract class MessageMapper {
    abstract fun toEntity(request: CreateMessageRequest): Message
    abstract fun toChatResponse(entity: Message): ChatResponse
    abstract fun toDto(entity: Message) : MessageDto

    @AfterMapping
    fun setSentAtAndUpdatedAt(@MappingTarget messageEntity : Message) {
        messageEntity.sentAt = Instant.now()
        messageEntity.updatedAt = Instant.now()
    }
}