package by.magofrays.service

import by.magofrays.dto.NotificationDto
import by.magofrays.entity.Notification
import by.magofrays.repository.NotificationRepository
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper


@Service
class KafkaSenderService(
    private val notificationRepository : NotificationRepository,
    private val notificationService: NotificationService,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(KafkaSenderService::class.java)

    @KafkaListener(topics = ["notification"],
        containerFactory = "kafkaListenerContainerFactory",
        groupId = "cohab.chat")
    fun notificationsConsumer(notificationString: String){
        log.info("Received notification consumer for $notificationString")
        val notificationDto = objectMapper.readValue(notificationString, NotificationDto::class.java)
        val notification = Notification(
            sender = notificationDto.from,
            message = notificationDto.message,
            recipient = notificationDto.recipient,
            createdAt = notificationDto.createdAt
        )
        notificationRepository.save(notification)
            .doOnError { error -> log.info("Failed to save: ${error.message}") }
            .subscribe()
        notificationService.sendNotification(notificationDto.recipient, notificationDto)
    }
}