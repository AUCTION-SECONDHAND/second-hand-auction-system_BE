package com.second_hand_auction_system.converters.notification;

import com.second_hand_auction_system.dtos.responses.notification.NotificationResponse;
import com.second_hand_auction_system.models.Notifications;
import org.springframework.stereotype.Component;

@Component
public class NotificationConverter {
    public NotificationResponse toNotificationResponse(Notifications notification) {
        return NotificationResponse.builder()
                .notificationId(notification.getNotificationId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .status(notification.getStatus())
                .notificationStatus(notification.getNotificationStatus())
                .build();
    }
}
