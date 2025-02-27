package com.second_hand_auction_system.service.auction;

import com.second_hand_auction_system.dtos.request.auction.AuctionDto;
import org.springframework.http.ResponseEntity;

public interface IAuctionService {
    void addAuction(AuctionDto auctionDto) throws Exception;

    void updateAuction(int auctionId,AuctionDto auctionDto) throws Exception;

    void removeAuction(int auctionId) throws Exception;

//    ResponseEntity<List<AuctionDto>> getAllAuctions(int page, int size);

    ResponseEntity<?> getAll();

    ResponseEntity<?> getAuctionById(Integer auctionId);


    long countAuctionsCreatedToday();

    ResponseEntity<?> countAuctionsByMonth();

    ResponseEntity<?> updateStatusOpen(Integer auctionId);

    ResponseEntity<?> updateStatusClose(Integer auctionId);
}
