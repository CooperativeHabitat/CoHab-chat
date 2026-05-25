package by.magofrays.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant


@Document(collection = "messages")
data class Message(
    @Id
    var id: String? = null,
    val content: String,
    val replyToId: String? = null,
    @Indexed
    var familyId: String,
    var memberId: String?,
    val multimediaUrl: List<String> = emptyList(),
    val reactions: List<Reaction> = emptyList(),
    val reads: List<MessageRead> = emptyList(),
    val sentAt: Instant = Instant.now(),
    var updatedAt: Instant = Instant.now()
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