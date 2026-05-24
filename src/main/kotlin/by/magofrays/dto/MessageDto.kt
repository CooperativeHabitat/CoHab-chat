package by.magofrays.dto

import by.magofrays.entity.MessageRead
import by.magofrays.entity.Reaction
import java.time.Instant

data class MessageDto(
    val content: String,
    val replyToId: String? = null,
    val multimediaUrl: List<String> = emptyList(),
    val reactions: List<Reaction> = emptyList(),
    val reads: List<MessageRead> = emptyList(),
    val sentAt: Instant,
    val updatedAt: Instant,
)
