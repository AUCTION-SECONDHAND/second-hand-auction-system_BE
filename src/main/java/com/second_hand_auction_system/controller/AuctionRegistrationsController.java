package com.second_hand_auction_system.controller;

import com.second_hand_auction_system.dtos.request.auctionRegistrations.AuctionRegistrationsDto;
import com.second_hand_auction_system.dtos.responses.ResponseListObject;
import com.second_hand_auction_system.dtos.responses.ResponseObject;
import com.second_hand_auction_system.dtos.responses.auctionRegistrations.AuctionRegistrationsResponse;
import com.second_hand_auction_system.dtos.responses.item.AuctionItemResponse;
import com.second_hand_auction_system.dtos.responses.item.ItemDetailResponse;
import com.second_hand_auction_system.service.auctionRegistrations.IAuctionRegistrationsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("")
    public ResponseEntity<?> getAuctionRegistrations(
            //@RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) throws Exception {
        PageRequest pageRequest = PageRequest.of(page, limit);
        //, Sort.by("id").descending()
        Page<AuctionRegistrationsResponse> auctionRegistrations = auctionRegistrationsService.findAllAuctionRegistrations(pageRequest);
        int totalPages = auctionRegistrations.getTotalPages();
        Long totalOrder = auctionRegistrations.getTotalElements();
        List<AuctionRegistrationsResponse> auctionRegistrationsResponses = auctionRegistrations.getContent();
        ResponseListObject<List<AuctionRegistrationsResponse>> responseListObject = ResponseListObject.<List<AuctionRegistrationsResponse>>builder()
                .data(auctionRegistrationsResponses)
                .totalElements(totalOrder)
                .totalPages(totalPages)
                .build();
        return ResponseEntity.ok(
                ResponseObject.builder()
                        .status(HttpStatus.OK)
                        .message("Success")
                        .data(responseListObject)
                        .build()
        );
    }

    @GetMapping("/user")
    public ResponseEntity<?> getAuctionRegistrationsByUser(
            //@RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) throws Exception {
        PageRequest pageRequest = PageRequest.of(page, limit);
        //, Sort.by("id").descending()
        Page<AuctionRegistrationsResponse> auctionRegistrations = auctionRegistrationsService.findAllAuctionRegistrationsByUserId(pageRequest);
        int totalPages = auctionRegistrations.getTotalPages();
        Long totalOrder = auctionRegistrations.getTotalElements();
        List<AuctionRegistrationsResponse> auctionRegistrationsResponses = auctionRegistrations.getContent();
        ResponseListObject<List<AuctionRegistrationsResponse>> responseListObject = ResponseListObject.<List<AuctionRegistrationsResponse>>builder()
                .data(auctionRegistrationsResponses)
                .totalElements(totalOrder)
                .totalPages(totalPages)
                .build();
        return ResponseEntity.ok(
                ResponseObject.builder()
                        .status(HttpStatus.OK)
                        .message("Success")
                        .data(responseListObject)
                        .build()
        );
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<?> getItemDetail(@PathVariable Integer id) throws Exception {
        AuctionRegistrationsResponse auctionRegistrationsResponse = auctionRegistrationsService.findAuctionRegistrationById(id);
        return ResponseEntity.ok(
                ResponseObject.builder()
                        .status(HttpStatus.OK)
                        .message("Success")
                        .data(auctionRegistrationsResponse)
                        .build()
        );
    }
}
