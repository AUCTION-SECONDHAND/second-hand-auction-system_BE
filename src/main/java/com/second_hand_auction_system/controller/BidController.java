package com.second_hand_auction_system.controller;

import com.second_hand_auction_system.dtos.request.bid.BidDto;
import com.second_hand_auction_system.dtos.responses.bid.BidResponses;
import com.second_hand_auction_system.service.bid.IBidService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/bids")
@RequiredArgsConstructor
public class BidController {


    private final IBidService bidService;


    @PostMapping
    public ResponseEntity<BidResponses> createBid(@RequestBody BidDto bidDto) throws Exception {
        BidResponses response = bidService.createBid(bidDto);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{bidId}")
    public ResponseEntity<BidResponses> updateBid(@PathVariable Integer bidId, @RequestBody BidDto bidDto) throws Exception {
        BidResponses response = bidService.updateBid(bidId, bidDto);
        return ResponseEntity.ok(response);
    }

    // Xóa một Bid
    @DeleteMapping("/{bidId}")
    public ResponseEntity<Void> deleteBid(@PathVariable Integer bidId) throws Exception {
        bidService.deleteBid(bidId);
        return ResponseEntity.noContent().build();
    }

    // Lấy thông tin của một Bid theo ID
    @GetMapping("/{bidId}")
    public ResponseEntity<BidResponses> getBidById(@PathVariable Integer bidId) throws Exception {
        // Gọi service để lấy BidResponses
        BidResponses response = bidService.getBidById(bidId);
        return ResponseEntity.ok(response);
    }

    // Lấy tất cả các Bid theo Auction ID
    @GetMapping("/auction/{auctionId}")
    public ResponseEntity<List<BidResponses>> getAllBidByAuctionId(@PathVariable Integer auctionId) throws Exception {
        // Gọi service để lấy danh sách BidResponses
        List<BidResponses> responseList = bidService.getAllBidsByAuctionId(auctionId);
        return ResponseEntity.ok(responseList);
    }

}