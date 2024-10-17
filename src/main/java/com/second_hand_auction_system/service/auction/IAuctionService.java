package com.second_hand_auction_system.service.auction;

import com.second_hand_auction_system.dtos.request.auction.AuctionDto;
import com.second_hand_auction_system.dtos.responses.auction.ListAuction;
import com.second_hand_auction_system.models.Auction;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface IAuctionService {
    void addAuction(AuctionDto auctionDto) throws Exception;
    void updateAuction(int auctionId,AuctionDto auctionDto) throws Exception;
    void removeAuction(int auctionId) throws Exception;


//    ResponseEntity<List<AuctionDto>> getAllAuctions(int page, int size);

//    ResponseEntity<?> getAll();
}
