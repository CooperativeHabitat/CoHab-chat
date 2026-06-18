package by.magofrays.repository

import by.magofrays.entity.Notification
import org.springframework.data.domain.Pageable
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

@Repository
interface NotificationRepository : R2dbcRepository<Notification, UUID> {

    fun findByRecipientAndCreatedAtBetweenOrderByCreatedAtDesc(
        recipient: UUID,
        startDate: Instant,
        endDate: Instant,
        limit: Int,
        offset: Long
    ): Flux<Notification>

    fun countByRecipientAndCreatedAtBetween(
        recipient: UUID,
        startDate: Instant,
        endDate: Instant
    ): Mono<Long>
}