import by.magofrays.dto.ClientMessage
import by.magofrays.dto.MessageDto
import org.springframework.http.MediaType
import org.springframework.http.codec.json.JacksonJsonDecoder
import org.springframework.http.codec.json.JacksonJsonEncoder
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.RSocketStrategies
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks
import java.net.URI
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
    val clientMessage = ClientMessage(
        memberId = UUID.randomUUID(),
        content = "Привет, семья!",
        replyToId = UUID.randomUUID()
    )
    val outgoing = Sinks.many().unicast().onBackpressureBuffer<ClientMessage>()
    val incomingMessages: Flux<MessageDto> = requester
        .route("api.family.chat.$familyId")
        .data(outgoing.asFlux())
        .retrieveFlux(MessageDto::class.java)

    incomingMessages
        .log()
        .doOnNext { msg -> println("Получено: $msg") }
        .doOnError { err -> println("Ошибка: $err") }
        .subscribe()

    while(true) {
        outgoing.tryEmitNext(clientMessage)
        Thread.sleep(1000)
    }
    requester.rsocket()?.dispose()
}