package com.second_hand_auction_system.controller;

import com.second_hand_auction_system.dtos.request.user.RegisterRequest;
import com.second_hand_auction_system.dtos.request.user.UserDto;
import com.second_hand_auction_system.dtos.responses.user.ListUserResponse;
import com.second_hand_auction_system.service.email.EmailService;
import com.second_hand_auction_system.service.user.IUserService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;
    private final EmailService emailService;
    @GetMapping("get-users")
    public ResponseEntity<ListUserResponse> getUser(){
        return userService.getListUser();
    }

    @PostMapping("/send-email")
    public String sendEmail(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String text,
            @RequestParam(required = false) MultipartFile[] files) throws MessagingException {
            return emailService.sendEmail(to, subject, text, files);

    }

    @PostMapping("/create-staff")
    public ResponseEntity<?> createStaff(@RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerStaff(registerRequest));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStaff(@PathVariable int id, @RequestBody UserDto userResponse) {
        return userService.updateUser(id,userResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable int id) {
        return userService.getUserId(id);
    }

}
