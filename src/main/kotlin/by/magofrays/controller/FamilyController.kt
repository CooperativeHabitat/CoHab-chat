package by.magofrays.controller

import by.magofrays.dto.NotificationDto
import by.magofrays.service.NotificationService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

@RestController
@RequestMapping("family")
class FamilyController(
    private val notificationService: NotificationService,
) {
    @GetMapping(path = ["notifications/connect/{memberId}"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun connectNotification(@PathVariable memberId: UUID) : Flux<NotificationDto> { // todo principal
        return notificationService.connectNotifications(memberId)
    }

    @GetMapping("/notifications/{memberId}")
    fun findAllNotificationById(
        @PathVariable memberId: UUID, // todo principal
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) startDate: Instant?,
        @RequestParam(required = false) endDate: Instant?
    ): Mono<Page<NotificationDto>> {
        val pageable = PageRequest.of(page, size)
        return notificationService.findAllNotificationByMember(memberId, startDate, endDate, pageable)
    }
}
