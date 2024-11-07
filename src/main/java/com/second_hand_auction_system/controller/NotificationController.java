package com.second_hand_auction_system.controller;

import com.second_hand_auction_system.service.notification.INotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final INotificationService notificationSerice;

    @PutMapping("closed-auction")
    public ResponseEntity<?> closedAuction(@RequestParam Integer auctionId) {
        return  notificationSerice.closeAuction(auctionId);
    }
}
