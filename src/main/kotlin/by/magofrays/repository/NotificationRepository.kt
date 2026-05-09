package by.magofrays.repository

import by.magofrays.entity.Notification
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Flux


interface NotificationRepository : ReactiveMongoRepository<Notification, String> {
    fun findByRecipient(recipient: String, pageable: Pageable): Flux<Notification>
}