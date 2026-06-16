package by.magofrays.controller

import by.magofrays.dto.NotificationDto
import by.magofrays.dto.NotificationRequest
import by.magofrays.service.NotificationService
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

@Controller
@MessageMapping("api.notification")
class NotificationController(
    private val notificationService: NotificationService,
) {

    suspend fun getMemberToken(): Jwt {
        return ReactiveSecurityContextHolder.getContext()
            .map { it.authentication?.principal as Jwt }
            .awaitSingle()
    }
    @MessageMapping("stream")
    suspend fun connectNotification() : Flux<NotificationDto> {
        val memberId = UUID.fromString(getMemberToken().subject)
        return notificationService.connectNotifications(memberId)
    }

    @MessageMapping("notifications")
    suspend fun findAllNotificationById(
        @Payload notificationRequest: NotificationRequest
    ): Page<NotificationDto> {
        val memberId = UUID.fromString(getMemberToken().subject)
        val pageable = PageRequest.of(notificationRequest.page,
            notificationRequest.size)
        return notificationService.findAllNotificationByMember(
            memberId,
            notificationRequest.startDate,
            notificationRequest.endDate,
            pageable)
    }
}
