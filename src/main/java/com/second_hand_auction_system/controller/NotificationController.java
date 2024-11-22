package com.second_hand_auction_system.controller;

import com.second_hand_auction_system.dtos.responses.ResponseObject;
import com.second_hand_auction_system.dtos.responses.notification.NotificationResponse;
import com.second_hand_auction_system.service.notification.INotificationService;
import com.second_hand_auction_system.sse.NotificationEventListener;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final INotificationService notificationSerice;
    private final NotificationEventListener notificationEventListener;

    @PutMapping("closed-auction")
    public ResponseEntity<?> closedAuction(@RequestParam Integer auctionId) {
        return notificationSerice.closeAuction(auctionId);
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter notificationStream() {
        return notificationEventListener.registerEmitter();
    }

    @GetMapping("")
    public ResponseEntity<?> notifications() throws Exception {
        List<NotificationResponse> notificationResponses = notificationSerice.getNotifications();
        return ResponseEntity.ok(
                ResponseObject.builder()
                        .status(HttpStatus.OK)
                        .message("Success")
                        .data(notificationResponses)
                        .build()
        );
    }
}
