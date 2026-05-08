package by.magofrays.service

import by.magofrays.dto.NotificationDto
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Service
class NotificationService
{
    class MemberSink(
        val sink: Sinks.Many<NotificationDto>,
        val subscribersCount: AtomicInteger
    )

    private val memberMap = ConcurrentHashMap<UUID, MemberSink>()


    fun connectNotification(memberId: UUID) : Flux<NotificationDto> {
        memberMap.computeIfAbsent(memberId) { MemberSink(
            sink = Sinks.many().multicast().onBackpressureBuffer(),
            subscribersCount = AtomicInteger(0)
        ) }
        val memberSink = memberMap[memberId]

        return memberSink!!.sink.asFlux()
            .doOnSubscribe { memberSink.subscribersCount.incrementAndGet() }
            .doOnCancel {
                val remaining = memberSink.subscribersCount.decrementAndGet()
                if(remaining < 1){
                    memberMap.remove(memberId)
                    memberSink.sink.tryEmitComplete()
                }
            }
    }

    fun sendNotification(memberId: UUID, notification: NotificationDto) {
        if(!memberMap.containsKey(memberId)){
            return
        }
        memberMap[memberId]?.sink?.tryEmitNext(notification)
    }


}