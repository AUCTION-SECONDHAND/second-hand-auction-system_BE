package com.second_hand_auction_system.controller;


import com.second_hand_auction_system.dtos.request.user.Authentication;
import com.second_hand_auction_system.dtos.request.user.RegisterRequest;
import com.second_hand_auction_system.dtos.responses.ResponseObject;
import com.second_hand_auction_system.dtos.responses.user.AuthenticationResponse;
import com.second_hand_auction_system.dtos.responses.user.RegisterResponse;
import com.second_hand_auction_system.service.user.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;


@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin("*")
public class AuthenticationController {

    private final IUserService userService;
    private final LogoutHandler logoutHandler;

    @PostMapping()
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        return userService.register(registerRequest);
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyOtp(@Valid @RequestParam String email, @RequestParam String otp) {
        return userService.isValidOtp(email, otp);

    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody Authentication authentication) {
        return userService.login(authentication);
    }

    @PostMapping("/refreshToken")
    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws Exception {
        userService.refresh(request, response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ResponseObject> logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            logoutHandler.logout(request, response, SecurityContextHolder.getContext().getAuthentication());
            SecurityContextHolder.clearContext();
            return ResponseEntity.ok(
                    ResponseObject.builder()
                            .status(HttpStatus.valueOf(HttpStatus.OK.value()))
                            .message("Logout successful")
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseObject.builder()
                            .status(HttpStatus.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                            .message("Logout failed")
                            .build()
            );
        }
    }

//    @PutMapping("/forgot-password")
//    public ResponseEntity<ForgotPasswordResponse> forgotPassword(@RequestParam String email) {
//        return new ResponseEntity<>(userService.forgotPassword(email), HttpStatus.OK);
//    }
//
//    @PatchMapping("/changePassword")
//    public ResponseEntity<ChangePassWordResponse> changePassword(@RequestBody ChangePassWordDTO request, Principal connectedUser) throws IllegalAccessException {
//        return userService.changePassword(request, connectedUser);
//    }
//
//    @PutMapping("/reset-password")
//    public ResponseEntity<ResetPassWordResponse> resetPassword(@RequestParam String email,
//                                                               @RequestParam String otp,
//                                                               @RequestHeader String newPassword) {
//        return  userService.resetPassword(email, otp,newPassword);
//    }


}
