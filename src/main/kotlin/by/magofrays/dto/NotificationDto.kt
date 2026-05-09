package by.magofrays.dto

import java.time.Instant
import java.util.UUID

data class NotificationDto (
    val from: String,
    val recipient: String,
    val message: String,
    val createdAt: Instant
)