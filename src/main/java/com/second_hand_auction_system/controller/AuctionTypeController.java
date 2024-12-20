package com.second_hand_auction_system.controller;

import com.second_hand_auction_system.dtos.request.auctiontype.AuctionTypeDTO;
import com.second_hand_auction_system.dtos.responses.ResponseObject;
import com.second_hand_auction_system.dtos.responses.auctionType.AuctionTypeResponse;
import com.second_hand_auction_system.models.AuctionType;
import com.second_hand_auction_system.repositories.AuctionTypeRepository;
import com.second_hand_auction_system.service.auctiontype.AuctionTypeSerivce;
import com.second_hand_auction_system.service.auctiontype.IAuctionTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
    @RequestMapping("/api/v1/auctionType")
@RequiredArgsConstructor
public class AuctionTypeController {
    private final IAuctionTypeService iAuctionTypeService;
    private final AuctionTypeSerivce auctionTypeSerivce;

    @PostMapping
    public ResponseEntity<?> createAuctionType(@Valid @RequestBody AuctionTypeDTO auctionType) {
        return iAuctionTypeService.createAuctionType(auctionType);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAuctionType(@Valid @RequestBody AuctionTypeDTO auctionType, @PathVariable int id) {
        return iAuctionTypeService.update(auctionType, id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAuctionType(@PathVariable int id) {
        return iAuctionTypeService.delete(id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAuctionType(@PathVariable int id) {
        return iAuctionTypeService.getById(id);
    }

    @GetMapping()
    public ResponseEntity<?> getAllAuctionTypes(@RequestParam("size") int size,
                                                @RequestParam("page") int page) {
        return iAuctionTypeService.getAuctions(size, page);
    }

    @GetMapping("/find-all")
    public ResponseEntity<?> getAllAuctionTypes() throws Exception {
        List<AuctionTypeResponse> auctionTypeResponses = auctionTypeSerivce.getAuctionTypes();
        return ResponseEntity.ok(
                ResponseObject.builder()
                        .status(HttpStatus.OK)
                        .message("Thành công")
                        .data(auctionTypeResponses)
                        .build()
        );
    }

}
