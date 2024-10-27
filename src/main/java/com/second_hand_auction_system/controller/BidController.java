package com.second_hand_auction_system.controller;

import com.second_hand_auction_system.dtos.request.bid.BidDto;
import com.second_hand_auction_system.dtos.request.bid.BidRequest;
import com.second_hand_auction_system.dtos.responses.bid.BidResponse;
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
    public ResponseEntity<?> createBid(@RequestBody BidRequest bidRequest) throws Exception {
        return bidService.createBid(bidRequest);
    }

    @PutMapping("/{bidId}")
    public ResponseEntity<BidResponse> updateBid(@PathVariable Integer bidId, @RequestBody BidDto bidDto) throws Exception {
        BidResponse response = bidService.updateBid(bidId, bidDto);
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
    public ResponseEntity<BidResponse> getBidById(@PathVariable Integer bidId) throws Exception {
        // Gọi service để lấy BidResponses
        BidResponse response = bidService.getBidById(bidId);
        return ResponseEntity.ok(response);
    }

    // Lấy tất cả các Bid theo Auction ID
    @GetMapping("/auction/{auctionId}")
    public ResponseEntity<List<BidResponse>> getAllBidByAuctionId(@PathVariable Integer auctionId) throws Exception {
        // Gọi service để lấy danh sách BidResponses
        List<BidResponse> responseList = bidService.getAllBidsByAuctionId(auctionId);
        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/find-winner/{auctionId}")
    public ResponseEntity<?> findWinner(@PathVariable int auctionId) throws Exception {
        return bidService.findWinnerAuction(auctionId);
    }

    @GetMapping("/history-bid/{auctionId}")
    public ResponseEntity<?> getAllBids(@PathVariable Integer auctionId,
                                                        @RequestParam(value = "limit", defaultValue = "10") int limit,
                                                        @RequestParam(value = "page", defaultValue = "0") int page) throws Exception {
        return bidService.getAllBids(auctionId,limit, page);
    }

}