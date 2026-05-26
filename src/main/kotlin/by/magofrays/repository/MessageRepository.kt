package by.magofrays.repository

import by.magofrays.entity.Message
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

@Repository
interface MessageRepository : ReactiveMongoRepository<Message, String> {

    fun findByFamilyIdAndSentAtBetween(familyId: String, startDate: Instant?, endDate: Instant?, pageable: Pageable): Flux<Message>
}