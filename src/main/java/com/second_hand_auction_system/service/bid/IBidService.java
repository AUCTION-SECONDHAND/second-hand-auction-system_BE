package com.second_hand_auction_system.service.bid;

import com.second_hand_auction_system.dtos.request.bid.BidDto;
import com.second_hand_auction_system.dtos.request.bid.BidRequest;
import com.second_hand_auction_system.dtos.responses.bid.BidResponse;
import com.second_hand_auction_system.models.Bid;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface IBidService {

    ResponseEntity<?> createBid(BidRequest bidRequest) throws Exception;

    ResponseEntity<?> createBidSealedBid(BidRequest bidRequest) throws Exception;

    ResponseEntity<?> updateBid(Integer bidId, BidRequest bidDto) throws Exception;

    ResponseEntity<?> deleteBid(Integer bidId) throws Exception;

    BidResponse getBidById(Integer bidId) throws Exception;

    List<BidResponse> getAllBidsByAuctionId(Integer auctionId) throws Exception;

    ResponseEntity<?> findWinnerAuction(int auctionId);

    ResponseEntity<?> getAllBids(Integer auctionId, int limit, int page);

    ResponseEntity<?> getInformationBid(Integer auctionId);

    ResponseEntity<?> getBidDetail(Integer auctionId);

    ResponseEntity<?> getHighestBid(Integer auctionId);
}
