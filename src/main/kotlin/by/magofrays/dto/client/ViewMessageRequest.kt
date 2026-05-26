package by.magofrays.dto.client

import java.util.UUID

data class ViewMessageRequest (
    val familyId: UUID,
    val messageId: UUID
)
