package by.magofrays.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

@Table("notifications")
data class Notification (
    @Id
    val id: UUID = UUID.randomUUID(),
    val from: String,
    val recipient: UUID,
    val message: String,
    val createdAt: Instant
)