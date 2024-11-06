package com.second_hand_auction_system.repositories;

import com.second_hand_auction_system.models.AuctionRegistrationUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuctionRegistrationUserRepository extends JpaRepository<AuctionRegistrationUser, Integer> {
    Optional<AuctionRegistrationUser> findByAuctionRegistration_Auction_AuctionIdAndUser_Id(Integer auctionId, Integer userId);
    Optional<AuctionRegistrationUser> findByAuctionRegistration_AuctionRegistrationId(Integer auctionRegistrationId);

}
