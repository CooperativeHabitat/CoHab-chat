package by.magofrays.repository

import by.magofrays.entity.Reaction
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ReactionRepository : ReactiveMongoRepository<Reaction, UUID>{

}