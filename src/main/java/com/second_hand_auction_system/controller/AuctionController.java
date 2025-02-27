package com.second_hand_auction_system.controller;

import com.second_hand_auction_system.dtos.request.auction.AuctionDto;
import com.second_hand_auction_system.dtos.responses.ResponseObject;
import com.second_hand_auction_system.dtos.responses.auction.ListAuction;
import com.second_hand_auction_system.models.Auction;
import com.second_hand_auction_system.service.auction.IAuctionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/auctions")
public class AuctionController {
    private final IAuctionService auctionService;

    @PostMapping("")
    public ResponseEntity<?> createAuction(
            @Valid @RequestBody AuctionDto auctionDto,
            BindingResult result
    ) throws Exception {
        if (result.hasErrors()) {
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(
                    ResponseObject.builder()
                            .status(HttpStatus.BAD_REQUEST)
                            .message(String.valueOf(errorMessages))
                            .build()
            );
        }
        auctionService.addAuction(auctionDto);
        return ResponseEntity.ok(
                ResponseObject.builder()
                        .status(HttpStatus.OK)
                        .message("Tạo đấu giá thành công")
                        .build()
        );
    }

    @PutMapping("/{auctionId}")
    public ResponseEntity<?> updateAuction(
            @PathVariable int auctionId,
            @Valid @RequestBody AuctionDto auctionDto,
            BindingResult result
    ) throws Exception {
        if (result.hasErrors()) {
            List<String> errorMessage = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
        }
        auctionService.updateAuction(auctionId, auctionDto);
        return ResponseEntity.ok(
                ResponseObject.builder()
                        .status(HttpStatus.OK)
                        .message("Cập nhật đấu giá thành công")
                        .build()
        );
    }

    @DeleteMapping("/{auctionId}")
    public ResponseEntity<?> deleteAuction(
            @PathVariable int auctionId
    ) throws Exception {
        auctionService.removeAuction(auctionId);
        return ResponseEntity.ok(
                ResponseObject.builder()
                        .status(HttpStatus.OK)
                        .message("Xóa thành công")
                        .build()
        );
    }

//    @GetMapping
//    public ResponseEntity<List<AuctionDto>> getAllAuctions(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size) {
//        return auctionService.getAllAuctions(page,size);
//    }

    @PutMapping("/update/{auctionId}")
    public ResponseEntity<?> updateAuction(@PathVariable Integer auctionId){
        return auctionService.updateStatusOpen(auctionId);
    }

    @PutMapping("/update-closed/{auctionId}")
    public ResponseEntity<?> updateAuctionClose(@PathVariable Integer auctionId){
        return auctionService.updateStatusClose(auctionId);
    }

    @GetMapping
    public ResponseEntity<?> getAuctions(){
        return auctionService.getAll();
    }

    @GetMapping("/{auctionId}")
    public ResponseEntity<?> getAuctionById(@PathVariable Integer auctionId) {
        return auctionService.getAuctionById(auctionId);
    }

    @GetMapping("/count-today")
    public ResponseEntity<Long> countAuctionsCreatedToday() {
        long count = auctionService.countAuctionsCreatedToday();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/count-month")
    public ResponseEntity<?> countAuctionsCreatedMonth() {
        return auctionService.countAuctionsByMonth();
    }



}
