package by.magofrays.dto

import java.time.Instant
import java.util.UUID

data class NotificationDto (
    val from: String,
    val recipient: UUID,
    val message: String,
    val createdAt: Instant
)