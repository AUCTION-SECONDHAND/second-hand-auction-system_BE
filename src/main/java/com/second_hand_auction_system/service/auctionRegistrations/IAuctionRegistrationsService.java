package com.second_hand_auction_system.service.auctionRegistrations;

import com.second_hand_auction_system.dtos.request.auctionRegistrations.AuctionRegistrationsDto;
import com.second_hand_auction_system.dtos.responses.auctionRegistrations.AuctionRegistrationsResponse;
import com.second_hand_auction_system.dtos.responses.auctionRegistrations.CheckStatusAuctionRegisterResponse;
import com.second_hand_auction_system.models.AuctionRegistration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface IAuctionRegistrationsService {
    ResponseEntity<?> addAuctionRegistration(AuctionRegistrationsDto auctionRegistrationsDto) throws Exception;

    void updateAuctionRegistration(int arId, AuctionRegistrationsDto auctionRegistrationsDto) throws Exception;

    void removeAuctionRegistration(int arId) throws Exception;
    Page<AuctionRegistrationsResponse> findAllAuctionRegistrations(PageRequest pageRequest) throws Exception;
    Page<AuctionRegistrationsResponse> findAllAuctionRegistrationsByUserId(PageRequest pageRequest) throws Exception;
    AuctionRegistrationsResponse findAuctionRegistrationById(int arId) throws Exception;
    List<CheckStatusAuctionRegisterResponse> getRegistrationsByUserId() throws Exception;
    CheckStatusAuctionRegisterResponse getRegistrationsByUserIdAnhAuctionId(Integer auctionId) throws Exception;

    Map<String, Object> checkUserInAuction(Integer auctionId) throws Exception;
     Page<AuctionRegistrationsResponse> findUsersRegisteredByAuctionId(Integer auctionId, Pageable pageable) throws Exception ;

}
