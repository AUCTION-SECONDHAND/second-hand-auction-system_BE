package com.second_hand_auction_system.service.notification;

import org.springframework.http.ResponseEntity;

public interface INotificationService {
    ResponseEntity<?> closeAuction(Integer auctionId);
}
