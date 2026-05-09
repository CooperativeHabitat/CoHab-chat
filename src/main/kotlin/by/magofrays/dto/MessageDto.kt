package by.magofrays.dto

import by.magofrays.entity.MessageRead
import by.magofrays.entity.Reaction
import java.time.Instant
import java.util.UUID

data class MessageDto(
    val content: String,
    val replyToId: UUID,
    val familyId: UUID,
    val memberId: UUID,
    val multimediaUrl: List<String>? = null,
    val reactions: List<Reaction>? = null,
    val reads: List<MessageRead>? = null,
    val sentAt: Instant,
    val updatedAt: Instant,
)
