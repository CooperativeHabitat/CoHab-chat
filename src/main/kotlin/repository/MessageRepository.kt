package by.magofrays.repository

import by.magofrays.entity.Message
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import java.util.UUID

interface MessageRepository : ReactiveMongoRepository<Message, UUID> {

}