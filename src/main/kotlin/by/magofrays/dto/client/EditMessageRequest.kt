package by.magofrays.dto.client

import by.magofrays.dto.MessageDto
import java.util.UUID

data class EditMessageRequest (
    val messageId : UUID,
    val messageDto : MessageDto,
    )