package by.magofrays.mapper

import by.magofrays.dto.NotificationDto
import by.magofrays.entity.Notification
import org.mapstruct.Mapper


@Mapper(componentModel = "spring")
abstract class NotificationMapper {
    abstract fun toDto(notification: Notification) : NotificationDto
}