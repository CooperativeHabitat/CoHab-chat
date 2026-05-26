package by.magofrays.dto.client

import java.util.UUID

data class EditMessageRequest(
    val messageId: UUID,
    val content: String,
    val familyId: UUID
)