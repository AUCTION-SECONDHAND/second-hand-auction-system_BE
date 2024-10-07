package com.second_hand_auction_system.service.auctionRegistrations;

import com.second_hand_auction_system.dtos.request.auctionRegistrations.AuctionRegistrationsDto;

public interface IAuctionRegistrationsService {
    void addAuctionRegistration(AuctionRegistrationsDto auctionRegistrationsDto) throws Exception;

    void updateAuctionRegistration(int arId, AuctionRegistrationsDto auctionRegistrationsDto) throws Exception;

    void removeAuctionRegistration(int arId) throws Exception;

}
