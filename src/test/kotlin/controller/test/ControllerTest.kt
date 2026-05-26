package controller.test

import by.magofrays.dto.ChatResponse
import by.magofrays.dto.MessageDto
import by.magofrays.dto.MessageRequest
import by.magofrays.dto.client.*
import org.springframework.http.MediaType
import org.springframework.http.codec.json.JacksonJsonDecoder
import org.springframework.http.codec.json.JacksonJsonEncoder
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.RSocketStrategies
import reactor.core.publisher.Flux
import java.net.URI
import java.time.Instant
import java.util.*

fun main() {
    val strategies = RSocketStrategies.builder()
        .encoder(JacksonJsonEncoder())
        .decoder(JacksonJsonDecoder())
        .build()

    val requester = RSocketRequester.builder()
        .rsocketStrategies(strategies)
        .dataMimeType(MediaType.APPLICATION_JSON)
        .connectWebSocket(URI.create("ws://localhost:7000/rsocket"))
        .block()!!

    val familyId = UUID.randomUUID()


    val chatStream: Flux<ChatResponse> = requester
        .route("api.family.chat.$familyId.stream")
        .retrieveFlux(ChatResponse::class.java)

    chatStream
        .doOnNext { msg -> println("\n>>> [${msg.operationType}] ${msg.memberId}: ${msg.content}") }
        .doOnError { err -> println("!!! Ошибка: ${err.message}") }
        .subscribe()

    Thread.sleep(1000)
    println("=== Подписались на чат ===\n")


    println("--- Отправка ---")
    requester
        .route("api.family.chat.send")
        .data(
            CreateMessageRequest(
                content = "Привет, семья!",
                familyId = familyId
            )
        )
        .send()
        .block()
    Thread.sleep(500)


    println("\n--- История ---")
    val messages: List<MessageDto> = requester
        .route("api.family.messages.$familyId")
        .data(
            MessageRequest(
                page = 0,
                size = 10,
                startDate = Instant.now().minusSeconds(86400),
                endDate = Instant.now()
            )
        )
        .retrieveFlux(MessageDto::class.java)
        .collectList()
        .block()!!

    println("Всего сообщений: ${messages.size}")
    var messageId: String? = null
    messages.forEach { msg ->
        println("  [${msg.messageId}] ${msg.content}")
        messageId = msg.messageId
    }

    if (messageId == null) {
        println("\nНет сообщений для тестирования остальных операций")
        requester.rsocket()?.dispose()
        return
    }


    println("\n--- Редактирование ---")
    requester
        .route("api.family.chat.edit")
        .data(
            EditMessageRequest(
                messageId = messageId,
                content = "Привет, семья! (отредактировано)",
                familyId = familyId
            )
        )
        .send()
        .block()
    Thread.sleep(500)

    println("\n--- Просмотр ---")
    requester
        .route("api.family.chat.view")
        .data(
            ViewMessageRequest(
                familyId = familyId,
                messageId = messageId
            )
        )
        .send()
        .block()
    Thread.sleep(500)

    println("\n--- Реакция ---")
    requester
        .route("api.family.chat.react")
        .data(
            ReactMessageRequest(
                messageId = messageId,
                reaction = "👍",
                familyId = familyId
            )
        )
        .send()
        .block()
    Thread.sleep(500)

    println("\n--- Удаление ---")
    requester
        .route("api.family.chat.delete")
        .data(
            DeleteMessageRequest(
                familyId = familyId,
                messageId = messageId
            )
        )
        .send()
        .block()
    Thread.sleep(500)

    println("\n=== Готово ===")
    println("Нажмите Enter для выхода...")
    readln()

    requester.rsocket()?.dispose()
}