package by.magofrays.configuration

import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories


@Configuration
@EnableReactiveMongoRepositories("by.magofrays.repository.mongo")
class MongoConfig : AbstractReactiveMongoConfiguration() {
    @Value("\${spring.data.mongodb.uri}")
    lateinit var mongoUri: String

    @Bean
    fun mongoClient(): MongoClient {
        return MongoClients.create(mongoUri)
    }

    protected override fun getDatabaseName(): String {
        return "reactive"
    }
}