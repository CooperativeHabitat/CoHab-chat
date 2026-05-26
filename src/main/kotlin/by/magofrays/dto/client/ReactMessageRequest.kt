package by.magofrays.dto.client

import java.util.UUID

data class ReactMessageRequest (
    val messageId : UUID,
    val reaction : String,
    val familyId: UUID
)