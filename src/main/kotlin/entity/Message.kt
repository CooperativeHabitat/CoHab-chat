package by.magofrays.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import java.util.UUID


@Document(collection = "messages")
class Message(
    @Id
    val id: String? = null,
    val content: String,
    val replyToId: UUID,
    @Indexed
    val familyId: UUID,
    val memberId: UUID,
    val multimediaUrl: Array<String>? = null,
    val sentAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)