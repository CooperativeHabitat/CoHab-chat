package by.magofrays.entity

import org.springframework.data.mongodb.core.mapping.Document
import java.util.UUID

@Document
class MessageNotification (
    val id: UUID,
    val senderId: UUID
    )