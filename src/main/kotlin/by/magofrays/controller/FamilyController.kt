package by.magofrays.controller

import by.magofrays.dto.NotificationDto
import by.magofrays.service.NotificationService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import java.util.*

@RestController
@RequestMapping("family")
class FamilyController(
    private val notificationService: NotificationService
) {
    @GetMapping(path = ["notifications/{memberId}"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun connectNotification(@PathVariable memberId: UUID) : Flux<NotificationDto> { // todo principal
        return notificationService.connectNotification(memberId);
    }
}