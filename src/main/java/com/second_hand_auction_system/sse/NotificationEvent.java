package com.second_hand_auction_system.sse;

import com.second_hand_auction_system.dtos.responses.notification.NotificationResponse;

import java.util.List;

public class NotificationEvent {
    private final List<NotificationResponse> notifications;

    public NotificationEvent(List<NotificationResponse> notifications) {
        this.notifications = notifications;
    }

    public List<NotificationResponse> getNotifications() {
        return notifications;
    }
}
