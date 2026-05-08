package by.magofrays.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import java.util.UUID

@Document(collection = "reactions")
data class Reaction(
    @Id
    val id: UUID,
    val memberId: UUID,
    val familyId: UUID,
    val messageId: UUID,
    val reaction: String,
    val timestamp: Instant
)
