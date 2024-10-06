package com.second_hand_auction_system.service.user;

import com.second_hand_auction_system.dtos.request.user.Authentication;
import com.second_hand_auction_system.dtos.request.user.RegisterRequest;
import com.second_hand_auction_system.dtos.responses.user.AuthenticationResponse;
import com.second_hand_auction_system.dtos.responses.user.ListUserResponse;
import com.second_hand_auction_system.dtos.responses.user.RegisterResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public interface IUserService {
    ResponseEntity<RegisterResponse> register(@Valid RegisterRequest registerRequest);

    ResponseEntity<AuthenticationResponse> login(@Valid Authentication authentication);

    void refresh(HttpServletRequest request, HttpServletResponse response) throws IOException;

    ResponseEntity<ListUserResponse> getListUser();



    ResponseEntity<?> isValidOtp(String email, String otp);
}
