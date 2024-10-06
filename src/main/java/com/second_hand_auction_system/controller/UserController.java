package com.second_hand_auction_system.controller;

import com.second_hand_auction_system.dtos.responses.user.ListUserResponse;
import com.second_hand_auction_system.service.email.EmailService;
import com.second_hand_auction_system.service.user.IUserService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;
    private final EmailService emailService;
    @GetMapping("getUser")
    @PreAuthorize("hasAuthority('admin:read')")
    public ResponseEntity<ListUserResponse> getUser(){
        return userService.getListUser();
    }

    @PostMapping("/send-email")
    public String sendEmail(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String text,
            @RequestParam(required = false) MultipartFile[] files) throws MessagingException {


            // Gọi service để gửi email và kiểm tra kết quả
            return emailService.sendEmail(to, subject, text, files);

    }

}
