package by.magofrays.service

import by.magofrays.dto.NotificationDto
import by.magofrays.entity.Notification
import by.magofrays.repository.NotificationRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service


@Service
class KafkaSenderService(
    private val notificationRepository : NotificationRepository,
    private val notificationService: NotificationService,
    private val objectMapper: ObjectMapper
) {
    @KafkaListener(topics = [$$"${kafka.topics.notification}"])
    fun notificationsConsumer(notificationString: String){
        val notificationDto = objectMapper.readValue(notificationString, NotificationDto::class.java)
        val notification = Notification(
            from = notificationDto.from,
            message = notificationDto.message,
            recipient = notificationDto.recipient.toString(),
            createdAt = notificationDto.createdAt
        )
        notificationRepository.save(notification)
            .doOnError { error -> println("Failed to save: ${error.message}") }
            .subscribe()
        notificationService.sendNotification(notificationDto.recipient, notificationDto)
    }
}