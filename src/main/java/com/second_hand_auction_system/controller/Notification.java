package com.second_hand_auction_system.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notification")
@RequiredArgsConstructor
public class Notification {
    private final SimpMessagingTemplate messagingTemplate;



    @PostMapping("/send-notification")
    public void sendNotification(@RequestBody String message) {
        messagingTemplate.convertAndSend("/topic/notifications", message);
    }
}
