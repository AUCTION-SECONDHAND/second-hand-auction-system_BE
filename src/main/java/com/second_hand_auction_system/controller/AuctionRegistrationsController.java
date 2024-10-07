package com.second_hand_auction_system.controller;

import com.second_hand_auction_system.dtos.request.auctionRegistrations.AuctionRegistrationsDto;
import com.second_hand_auction_system.dtos.responses.ResponseObject;
import com.second_hand_auction_system.service.auctionRegistrations.IAuctionRegistrationsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/auction-register")
public class AuctionRegistrationsController {
    private final IAuctionRegistrationsService auctionRegistrationsService;

    @PostMapping("")
    public ResponseEntity<?> createAuctionRegister(
            @Valid @RequestBody AuctionRegistrationsDto auctionRegistrationsDto,
            BindingResult result
    ) throws Exception {
        if (result.hasErrors()) {
            List<String> errors = result.getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(
                    ResponseObject.builder()
                            .status(HttpStatus.BAD_REQUEST)
                            .message(String.valueOf(errors))
                            .build()
            );
        }
        auctionRegistrationsService.addAuctionRegistration(auctionRegistrationsDto);
        return ResponseEntity.ok(
                ResponseObject.builder()
                        .status(HttpStatus.OK)
                        .message("Success")
                        .build()
        );
    }
}
