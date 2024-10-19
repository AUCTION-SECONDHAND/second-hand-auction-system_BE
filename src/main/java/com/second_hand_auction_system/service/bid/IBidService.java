package com.second_hand_auction_system.service.bid;

import com.second_hand_auction_system.dtos.request.bid.BidDto;
import com.second_hand_auction_system.dtos.responses.bid.BidResponses;

import java.util.List;

public interface IBidService {

    BidResponses createBid(BidDto bidDto) throws Exception;

    BidResponses updateBid(Integer bidId, BidDto bidDto) throws Exception;

    void deleteBid(Integer bidId) throws Exception;

    BidResponses getBidById(Integer bidId) throws Exception;

    List<BidResponses> getAllBidsByAuctionId(Integer auctionId) throws Exception;
}
