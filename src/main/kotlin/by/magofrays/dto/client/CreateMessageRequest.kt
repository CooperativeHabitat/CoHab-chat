package by.magofrays.dto.client

import java.util.UUID

data class CreateMessageRequest(
    val content: String,
    val replyToId: String? = null,
    val familyId: UUID,
)
