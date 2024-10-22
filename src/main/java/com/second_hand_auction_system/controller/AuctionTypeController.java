package com.second_hand_auction_system.controller;

import com.second_hand_auction_system.dtos.request.auctiontype.AuctionTypeDTO;
import com.second_hand_auction_system.models.AuctionType;
import com.second_hand_auction_system.repositories.AuctionTypeRepository;
import com.second_hand_auction_system.service.auctiontype.IAuctionTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auctionType")
@RequiredArgsConstructor
public class AuctionTypeController {
    private final IAuctionTypeService iAuctionTypeService;

    @PostMapping
    public ResponseEntity<?> createAuctionType (@Valid  @RequestBody AuctionTypeDTO auctionType) {
        return iAuctionTypeService.createAuctionType(auctionType);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAuctionType (@Valid  @RequestBody AuctionTypeDTO auctionType,@PathVariable int id) {
        return iAuctionTypeService.update(auctionType,id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAuctionType (@PathVariable int id) {
        return iAuctionTypeService.delete(id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAuctionType (@PathVariable int id) {
        return iAuctionTypeService.getById(id);
    }

    @GetMapping()
    public ResponseEntity<?> getAllAuctionTypes(@RequestParam("size") int size,
                                                @RequestParam("page") int page) {
        return iAuctionTypeService.getAuctions(size,page);
    }

}
