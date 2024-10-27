package com.second_hand_auction_system.converters.bid;

import com.second_hand_auction_system.dtos.request.bid.BidDto;
import com.second_hand_auction_system.dtos.responses.bid.BidResponse;
import com.second_hand_auction_system.models.Auction;
import com.second_hand_auction_system.models.Bid;
import com.second_hand_auction_system.models.User;


public class BidConverter {
    // Converter BidDto => Bid entity
    public static Bid convertToEntity(BidDto bidDto, User user, Auction auction) {
        Bid bid = new Bid();
//        bid.setBidId(bidDto.getBidId());
        bid.setBidAmount(bidDto.getBidAmount());
//        bid.setBidTime(bidDto.getBidTime());
//        bid.setBidStatus(bidDto.getBidStatus());
        bid.setWinBid(bidDto.isWinBid());
        bid.setUser(user);
        bid.setAuction(auction);
        return bid;
    }

    // Converter Bid entity => BidResponses
    public static BidResponse convertToResponse(Bid bid) {
        return BidResponse.builder()
                .bidId(bid.getBidId())
                .bidAmount(bid.getBidAmount())
//                .bidTime(bid.getBidTime())
//                .bidStatus(bid.getBidStatus())
//                .winBid(bid.isWinBid())
                .userId(bid.getUser().getId())
                .username(bid.getUser().getFullName())
                .auctionId(bid.getAuction().getAuctionId())
                .build();
    }
}
