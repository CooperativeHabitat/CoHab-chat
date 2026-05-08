package by.magofrays.repository

import by.magofrays.entity.Message
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import java.util.UUID

@Repository
interface MessageRepository : ReactiveMongoRepository<Message, UUID> {

    fun findByFamilyId(familyId: UUID): Flux<Message>
}