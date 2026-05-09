package by.magofrays.unit

import by.magofrays.dto.NotificationDto
import by.magofrays.service.NotificationService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import reactor.test.StepVerifier
import java.time.Duration
import java.time.Instant
import java.util.UUID

class NotificationServiceTest {

    private val notificationService = NotificationService()

    @Test
    fun `should receive notification after subscription`() {
        val memberId = UUID.randomUUID()

        val notification = NotificationDto(
            from = "taskService",
            recipient = UUID.randomUUID(),
            message = "Test message",
            createdAt = Instant.now()
        )

        val flux = notificationService.connectNotification(memberId)

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
    fun `should support multiple subscribers`() {
        val memberId = UUID.randomUUID()


        val notification = NotificationDto(
            from = "taskService",
            recipient = UUID.randomUUID(),
            message = "Test message",
            createdAt = Instant.now()
        )

        val flux1 = notificationService.connectNotification(memberId)
        val flux2 = notificationService.connectNotification(memberId)
        val flux3 = notificationService.connectNotification(memberId)

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

        verifier1.verify(Duration.ofSeconds(3))
        verifier2.verify(Duration.ofSeconds(3))
        verifier3.verify(Duration.ofSeconds(3))

    }


    @Test
    fun `should cleanup sink after all subscribers disconnected`() {
        val memberId = UUID.randomUUID()

        val flux = notificationService.connectNotification(memberId)

        StepVerifier.create(flux)
            .thenCancel()
            .verify()

        val field = NotificationService::class.java.getDeclaredField("memberMap")
        field.isAccessible = true

        val memberMap = field.get(notificationService) as Map<*, *>

        assertFalse(memberMap.containsKey(memberId))
    }
}