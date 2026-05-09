package by.magofrays.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant


@Document(collection = "messages")
data class Message(
    @Id
    val id: String? = null,
    val content: String,
    val replyToId: String,
    @Indexed
    val familyId: String,
    val memberId: String,
    val multimediaUrl: List<String>? = null,
    val reactions: List<Reaction>? = null,
    val reads: List<MessageRead>? = null,
    val sentAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)

data class MessageRead (
    val messageId: String,
    val memberId: String,
    val readAt: Instant = Instant.now()
)

data class Reaction(
    val memberId: String,
    val familyId: String,
    val messageId: String,
    val reaction: String,
    val timestamp: Instant = Instant.now()
)