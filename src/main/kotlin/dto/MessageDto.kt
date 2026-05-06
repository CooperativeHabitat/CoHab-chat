package by.magofrays.dto

import java.time.Instant
import java.util.UUID

data class MessageDto(val id: UUID,
                      val content: String,
                      val replyToId: UUID,
                      val familyId: UUID,
                      val memberId: UUID,
                      val multimediaUrl: Array<String>,
                      val readBy: Array<UUID>,
                      val sentAt: Instant)
