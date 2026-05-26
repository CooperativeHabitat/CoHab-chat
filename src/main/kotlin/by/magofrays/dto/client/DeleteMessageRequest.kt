package by.magofrays.dto.client

import java.util.UUID

data class DeleteMessageRequest(
    val familyId: UUID,
    val messageId: UUID
)
