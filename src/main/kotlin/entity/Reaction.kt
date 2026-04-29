package by.magofrays.entity

import org.springframework.data.mongodb.core.mapping.Document
import java.util.UUID

@Document
class Reaction(
    val memberId: UUID,
    val familyId: UUID,
    val messageId: UUID,
    val reaction: String
)
