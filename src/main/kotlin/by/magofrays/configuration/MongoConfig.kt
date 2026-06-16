package by.magofrays.configuration

import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories


@Configuration
@EnableReactiveMongoRepositories("by.magofrays.repository.mongo")
class MongoConfig : AbstractReactiveMongoConfiguration() {
    private val log = LoggerFactory.getLogger(this::class.java)
    @Value("\${spring.data.mongodb.uri}")
    lateinit var mongoUri: String

    @Bean
    fun mongoClient(): MongoClient {
        log.info("Connecting to mongo via $mongoUri")
        return MongoClients.create(mongoUri)
    }

    protected override fun getDatabaseName(): String {
        return "reactive"
    }
}