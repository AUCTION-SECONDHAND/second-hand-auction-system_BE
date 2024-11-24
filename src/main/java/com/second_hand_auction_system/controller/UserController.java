package com.second_hand_auction_system.controller;

import com.second_hand_auction_system.dtos.request.user.ChangePassWordDTO;
import com.second_hand_auction_system.dtos.request.user.RegisterRequest;
import com.second_hand_auction_system.dtos.request.user.UserDto;
import com.second_hand_auction_system.dtos.responses.user.ListUserResponse;
import com.second_hand_auction_system.service.email.EmailService;
import com.second_hand_auction_system.service.user.IUserService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final IUserService userService;
    private final EmailService emailService;
    private final SimpMessagingTemplate messagingTemplate;
    @GetMapping("get-users")
    public ResponseEntity<?> getUser(@RequestParam(value = "page",defaultValue = "0") int page,
                                                    @RequestParam(value = "limit",defaultValue = "10") int limit){
        return userService.getListUser(page,limit);
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

    @PutMapping("")
    public ResponseEntity<?> updateStaff( @RequestBody UserDto userResponse) {
        return userService.updateUser(userResponse);
    }

    @GetMapping("")
    public ResponseEntity<?> getUserById() {
        return userService.getUserId();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable int id) {
        return userService.deleteUser(id);
    }

    @PutMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) throws MessagingException {
        return userService.forgotPassword(email);
    }

    @PatchMapping("/changePassword")
    public ResponseEntity<?> changePassword(@RequestBody ChangePassWordDTO request, Principal connectedUser) throws IllegalAccessException {
        return userService.changePassword(request, connectedUser);
    }

    @GetMapping("/comparison")
    public ResponseEntity<Map<String, Object>> getUserComparison() {
        Map<String, Object> comparison = userService.getUserComparison();
        return ResponseEntity.ok(comparison);
    }

    @GetMapping("/seller-by-week")
    public ResponseEntity<?> getUserByWeek() {
        return userService.countSellerByWeek();
    }



}
