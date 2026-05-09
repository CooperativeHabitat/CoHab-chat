package by.magofrays.service

import by.magofrays.dto.NotificationDto
import by.magofrays.repository.NotificationRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import tools.jackson.databind.ObjectMapper
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Service
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val objectMapper: ObjectMapper
)
{
    class MemberSink(
        val sink: Sinks.Many<NotificationDto>,
        val subscribersCount: AtomicInteger
    )

    private val memberMap = ConcurrentHashMap<UUID, MemberSink>()
    private val log = LoggerFactory.getLogger(NotificationService::class.java)

    fun connectNotification(memberId: UUID) : Flux<NotificationDto> {
        log.info("Connecting client to $memberId sink")
        memberMap.computeIfAbsent(memberId) { MemberSink(
            sink = Sinks.many().multicast().onBackpressureBuffer(1, false),
            subscribersCount = AtomicInteger(0)
        ) }
        val memberSink = memberMap[memberId]
        memberSink!!.subscribersCount.incrementAndGet()
        return memberSink.sink.asFlux()
            .doOnCancel {
                log.info("One of clients is unsubscribed from $memberId sink")
                val remaining = memberSink.subscribersCount.decrementAndGet()
                if(remaining < 1){
                    log.info("Removing sink for $memberId")
                    memberMap.remove(memberId)
                    memberSink.sink.tryEmitComplete()
                    log.info("Removed sink for $memberId")
                }
            }
            .doOnError {
                error -> log.info("Error occurred in sink: ${error.message}")
            }
    }

    fun sendNotification(memberId: UUID, notification: NotificationDto) {
        if(!memberMap.containsKey(memberId)){
            return
        }
        log.info("Sending notification to $memberId sink")
        memberMap[memberId]?.sink?.tryEmitNext(notification)
    }

    fun findAllNotificationByMember(memberId: UUID, startDate: Instant?, endDate: Instant?, pageable: Pageable) : Mono<Page<NotificationDto>> {
        return notificationRepository.findByRecipientAndCreatedAtBetween(memberId, startDate, endDate, pageable)
            .map { notification -> objectMapper.convertValue(notification, NotificationDto::class.java) }
            .collectList()
            .zipWith(notificationRepository.countByRecipientAndCreatedAtBetween(memberId, startDate, endDate))
            .map{p -> PageImpl(p.getT1(), pageable, p.getT2()) }
    }
}