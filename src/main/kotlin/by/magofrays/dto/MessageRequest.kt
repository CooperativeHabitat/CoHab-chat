package by.magofrays.dto

import java.time.Instant

data class MessageRequest(
    val page: Int = 0,
    val size: Int = 20,
    val startDate: Instant,
    val endDate: Instant
)