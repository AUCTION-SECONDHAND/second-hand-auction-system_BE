package com.second_hand_auction_system.service.auction;

import com.second_hand_auction_system.dtos.request.auction.AuctionDto;

public interface IAuctionService {
    void addAuction(AuctionDto auctionDto) throws Exception;
    void updateAuction(int auctionId,AuctionDto auctionDto) throws Exception;
    void removeAuction(int auctionId) throws Exception;

}
