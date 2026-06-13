package by.magofrays.repository.mongo

import by.magofrays.entity.Message
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import java.time.Instant

@Repository
interface MessageRepository : ReactiveMongoRepository<Message, String> {
    fun findByFamilyIdAndSentAtBetweenOrderBySentAtDesc(
        familyId: String,
        startDate: Instant,
        endDate: Instant,
        pageable: Pageable
    ): Flux<Message>
}