package by.magofrays.dto

import java.util.UUID

data class ClientMessage(
    val memberId: UUID,
    val content: String,
    val replyToId: UUID
)