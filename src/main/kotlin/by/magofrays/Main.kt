package by.magofrays

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories

@SpringBootApplication
@EnableReactiveMongoRepositories("by.magofrays.repository.mongo")
class App

fun main(args: Array<String>) {
    runApplication<App>(*args)
}

