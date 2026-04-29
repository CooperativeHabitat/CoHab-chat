package by.magofrays.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.UUID


@Document(collection = "messages")
class Message(
    @Id
    val id: UUID,
    val message: String,
    val replyToId: UUID,
    val familyId: UUID,
    val memberId: UUID,
    )