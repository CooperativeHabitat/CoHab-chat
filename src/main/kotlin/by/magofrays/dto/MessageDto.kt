package by.magofrays.dto

import java.time.Instant
import java.util.UUID

data class MessageDto(
    val content: String,
    val replyToId: UUID,
    val familyId: UUID,
    val memberId: UUID,
    val multimediaUrl: Array<String>? = null,
    val sentAt: Instant,
    val updatedAt: Instant,
)
