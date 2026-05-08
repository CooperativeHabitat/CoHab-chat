package by.magofrays.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "notifications")
data class Notification (
    @Id
    val id: String? = null,
    val from: String,
    @Indexed
    val recipient: String,
    val message: String,
    val createdAt: Instant
)