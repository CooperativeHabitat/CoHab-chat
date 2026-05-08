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
    val multimediaUrl: Array<String>? = null,
    val sentAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)