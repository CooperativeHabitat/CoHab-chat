package by.magofrays.unit

import by.magofrays.dto.NotificationDto
import by.magofrays.entity.Notification
import by.magofrays.repository.NotificationRepository
import by.magofrays.service.NotificationService
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.junit.jupiter.SpringExtension
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import tools.jackson.databind.ObjectMapper
import java.time.Instant
import java.util.*

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [NotificationService::class, ObjectMapper::class])
class NotificationServiceTest {

    @Autowired
    lateinit var notificationService: NotificationService

    @MockitoBean
    lateinit var notificationRepository: NotificationRepository


    @Test
    fun `should receive notification after subscription`() {
        val memberId = UUID.randomUUID()

        val notification = NotificationDto(
            from = "taskService",
            recipient = UUID.randomUUID(),
            message = "Test message",
            createdAt = Instant.now()
        )

        val flux = notificationService.connectNotifications(memberId)

        StepVerifier.create(flux)
            .then {
                notificationService.sendNotification(memberId, notification)
            }
            .expectNext(notification)
            .thenCancel()
            .verify()
    }

    @Test
    fun `should not emit anything if no subscribers`() {
        val memberId = UUID.randomUUID()
        val notification = NotificationDto(
            from = "taskService",
            recipient = UUID.randomUUID(),
            message = "Test message",
            createdAt = Instant.now()
        )

        assertDoesNotThrow {
            notificationService.sendNotification(memberId, notification)
        }
    }

    @Test
    fun `should support multiple subscribers sequentially`() {
        val memberId = UUID.randomUUID()


        val notification = NotificationDto(
            from = "taskService",
            recipient = UUID.randomUUID(),
            message = "Test message",
            createdAt = Instant.now()
        )

        val flux1 = notificationService.connectNotifications(memberId)
        val flux2 = notificationService.connectNotifications(memberId)
        val flux3 = notificationService.connectNotifications(memberId)

        val verifier1 = StepVerifier.create(flux1)
            .then {
                notificationService.sendNotification(memberId, notification)
            }
            .expectNext(notification)
            .thenCancel()

        val verifier2 = StepVerifier.create(flux2)
            .then {
                notificationService.sendNotification(memberId, notification)
            }
            .expectNext(notification)
            .thenCancel()

        val verifier3 = StepVerifier.create(flux3)
            .then {
                notificationService.sendNotification(memberId, notification)
            }
            .expectNext(notification)
            .thenCancel()

        verifier1.verify()
        verifier2.verify()
        verifier3.verify()
    }

    @Test
    fun `should support multiple subscribers parallel`() {
        val memberId = UUID.randomUUID()
        val notification = NotificationDto(
            from = "taskService",
            recipient = UUID.randomUUID(),
            message = "Test message",
            createdAt = Instant.now()
        )
        val flux1 = notificationService.connectNotifications(memberId)
        val flux2 = notificationService.connectNotifications(memberId)
        val flux3 = notificationService.connectNotifications(memberId)
        val result = Flux.merge(flux1, flux2, flux3 )
        val verifier = StepVerifier.create(result)
            .then {
                notificationService.sendNotification(memberId, notification)
            }
            .expectNext(notification)
            .expectNext(notification)
            .expectNext(notification)
            .thenCancel()
        verifier.verify()
    }

    @Test
    fun `should cleanup sink after all subscribers disconnected`() {
        val memberId = UUID.randomUUID()
        val flux = notificationService.connectNotifications(memberId)
        StepVerifier.create(flux)
            .thenCancel()
            .verify()
        val field = NotificationService::class.java.getDeclaredField("memberMap")
        field.isAccessible = true
        val memberMap = field.get(notificationService) as Map<*, *>
        assertFalse(memberMap.containsKey(memberId))
    }


    @Test
    fun `should return page of notifications`(){
        val recipientId = UUID.randomUUID()
        val pageable = PageRequest.of(0, 3)
        val startDate = Instant.now().minusSeconds(3600)
        val endDate = Instant.now()
        val notifications = (1..3).map { index ->
            Notification(
                from = "taskService",
                recipient = recipientId,
                message = "Test message $index",
                createdAt = Instant.now().plusSeconds(index.toLong())
            )
        }

        val notificationDtos = notifications.map { notification ->
            NotificationDto(
                from = notification.from,
                recipient = notification.recipient,
                message = notification.message,
                createdAt = notification.createdAt
            )
        }
        Mockito.`when`(notificationRepository.findByRecipientAndCreatedAtBetween(recipientId, startDate, endDate, pageable))
            .thenReturn(Flux.fromIterable(notifications))
        Mockito.`when`(notificationRepository.countByRecipientAndCreatedAtBetween(recipientId, startDate, endDate))
            .thenReturn(Mono.just(3L))
        val expected = PageImpl(notificationDtos, pageable, 3L)
        val monoResult = notificationService.findAllNotificationByMember(recipientId, startDate, endDate, pageable)
        val verifier = StepVerifier.create(monoResult)
            .expectNext(expected)
        verifier.verifyComplete()
    }
}