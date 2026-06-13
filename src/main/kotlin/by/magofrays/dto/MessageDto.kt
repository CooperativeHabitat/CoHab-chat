package by.magofrays.dto

import by.magofrays.entity.MessageRead
import by.magofrays.entity.Reaction
import java.time.Instant
import java.util.UUID

data class MessageDto(
    val memberId: String,
    val messageId: String,
    val content: String,
    val replyToId: String?,
    val reactions: List<Reaction>?,
    val reads: List<MessageRead>?,
    val sentAt: Instant,
    val updatedAt: Instant,
)
