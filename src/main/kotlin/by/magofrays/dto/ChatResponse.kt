package by.magofrays.dto

import java.time.Instant

data class ChatResponse (
    val familyId: String,
    val memberId: String,
    val messageId: String,
    val messageDto: MessageDto?,
    val reaction: String?,
    val operationType: ChatOperationType,
    val timestamp: Instant = Instant.now(),
) {
    enum class ChatOperationType {
        NEW_MESSAGE,
        EDIT_MESSAGE,
        DELETE_MESSAGE,
        REACTION_ON_MESSAGE,
        VIEWED_MESSAGE
    }
}