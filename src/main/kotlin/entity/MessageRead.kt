package by.magofrays.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import java.util.UUID

@Document(collection = "message_reads")
class MessageRead (
    @Id
    val id: UUID,
    val messageId: UUID,
    val memberId: UUID,
    val readAt: Instant = Instant.now()
    )