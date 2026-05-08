package by.magofrays.repository

import by.magofrays.entity.Notification
import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface NotificationRepository : ReactiveMongoRepository<Notification, String> {
}