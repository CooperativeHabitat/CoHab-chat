package by.magofrays.dto

import by.magofrays.entity.MessageRead
import by.magofrays.entity.Reaction
import java.time.Instant

data class ChatResponse (
    val familyId: String,
    val memberId: String,
    val messageId: String,
    val reactions: List<Reaction>?,
    val reads: List<MessageRead>?,
    var operationType: ChatOperationType?,
    val sentAt: Instant,
    val updatedAt: Instant
) {
    enum class ChatOperationType {
        NEW_MESSAGE,
        EDIT_MESSAGE,
        DELETE_MESSAGE,
        REACTION_ON_MESSAGE,
        VIEWED_MESSAGE
    }
}