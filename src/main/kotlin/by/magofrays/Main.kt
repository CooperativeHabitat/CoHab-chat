package by.magofrays

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.mongodb.autoconfigure.MongoReactiveAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication(exclude = [MongoReactiveAutoConfiguration::class])
class App

fun main(args: Array<String>) {
    runApplication<App>(*args)
}

