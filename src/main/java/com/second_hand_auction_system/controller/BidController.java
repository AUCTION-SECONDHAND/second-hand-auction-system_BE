package com.second_hand_auction_system.controller;

import com.second_hand_auction_system.dtos.request.bid.BidDto;
import com.second_hand_auction_system.dtos.request.bid.BidRequest;
import com.second_hand_auction_system.dtos.responses.ResponseObject;
import com.second_hand_auction_system.dtos.responses.bid.BidResponse;
import com.second_hand_auction_system.service.bid.BidService;
import com.second_hand_auction_system.service.bid.IBidService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/bids")
@RequiredArgsConstructor
public class BidController {

    private final SimpMessagingTemplate messagingTemplate;

    private final IBidService bidService;

    private final BidService bidServices;


    @PostMapping
    public ResponseEntity<?> createBid(@RequestBody BidRequest bidRequest) throws Exception {
        return bidService.createBid(bidRequest);
    }

    @PostMapping("/create/Sealed")
    public ResponseEntity<?> createBidSealedBid(@RequestBody BidRequest bidRequest) {
        try {
            return bidService.createBidSealedBid(bidRequest);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseObject.builder()
                            .data(null)
                            .message("Đã xảy ra lỗi: " + e.getMessage())
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .build()
            );
        }
    }



    @PutMapping("/{bidId}")
    public ResponseEntity<?> updateBid(@PathVariable Integer bidId, @RequestBody BidRequest bidDto) throws Exception {
        return bidService.updateBid(bidId, bidDto);
    }

    // Xóa một Bid
    @DeleteMapping("/{bidId}")
    public ResponseEntity<?> deleteBid(@PathVariable Integer bidId) throws Exception {
        return bidService.deleteBid(bidId);

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
        return bidService.getAllBids(auctionId, limit, page);
    }

    @GetMapping("information-bid/{auctionId}")
    public ResponseEntity<?> getBidInformationById(@PathVariable Integer auctionId) throws Exception {
        return bidService.getInformationBid(auctionId);
    }

    @GetMapping("/detail/{auctionId}")
    public ResponseEntity<?> getBidDetail(@PathVariable Integer auctionId) throws Exception {
        return bidService.getBidDetail(auctionId);
    }

    @GetMapping("/highest-bid/{auctionId}")
    public ResponseEntity<?> getHighestBid(@PathVariable Integer auctionId) throws Exception {
       return bidService.getHighestBid(auctionId);
    }

    @GetMapping("/stream-bids")
    public SseEmitter streamBids() {
        SseEmitter emitter = new SseEmitter(0L); // Không timeout
        // Đăng ký emitter vào danh sách lưu trữ (để gửi dữ liệu tới tất cả client khi có cập nhật)
        bidServices.addEmitter(emitter);
        emitter.onCompletion(() -> bidServices.removeEmitter(emitter));
        emitter.onTimeout(() -> bidServices.removeEmitter(emitter));
        emitter.onError((e) -> bidServices.removeEmitter(emitter));
        return emitter;
    }
}