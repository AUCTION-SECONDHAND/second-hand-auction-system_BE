package com.second_hand_auction_system.service.notification;

import com.second_hand_auction_system.dtos.responses.notification.NotificationResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface INotificationService {
    ResponseEntity<?> closeAuction(Integer auctionId);
    void createBidNotification(Integer userId, String title, String message);
    List<NotificationResponse> getNotifications() throws Exception;
}
