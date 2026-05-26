package by.magofrays.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant


@Document(collection = "messages")
data class Message(
    @Id
    var messageId: String? = null,
    var content: String,
    val replyToId: String? = null,
    @Indexed
    var familyId: String,
    var memberId: String?,
    var reactions: List<Reaction>?,
    var reads: List<MessageRead>?,
    var sentAt: Instant?,
    var updatedAt: Instant?
)

data class MessageRead (
    val memberId: String,
    val readAt: Instant = Instant.now()
)

data class Reaction(
    val memberId: String,
    val reaction: String,
    val reactedAt: Instant = Instant.now()
)