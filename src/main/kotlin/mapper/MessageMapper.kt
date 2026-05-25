package mapper

import by.magofrays.dto.ChatResponse
import by.magofrays.dto.client.CreateMessageRequest
import by.magofrays.entity.Message
import by.magofrays.entity.MessageRead
import org.mapstruct.Mapper

@Mapper(componentModel = "spring")
abstract class MessageMapper {
    abstract fun toEntity(request: CreateMessageRequest) : Message
    abstract fun toChatResponse(entity: Message) : ChatResponse
}