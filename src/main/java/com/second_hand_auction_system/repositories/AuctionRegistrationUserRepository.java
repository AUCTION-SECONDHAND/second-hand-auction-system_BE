package com.second_hand_auction_system.repositories;

import com.second_hand_auction_system.models.AuctionRegistrationUser;
import com.second_hand_auction_system.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AuctionRegistrationUserRepository extends JpaRepository<AuctionRegistrationUser, Integer> {
    Optional<AuctionRegistrationUser> findByAuctionRegistration_Auction_AuctionIdAndUser_Id(Integer auctionId, Integer userId);

    Optional<AuctionRegistrationUser> findByAuctionRegistration_AuctionRegistrationId(Integer auctionRegistrationId);

    @Query("SELECT u FROM User u JOIN AuctionRegistration ar ON u MEMBER OF ar.users " +
            "WHERE ar.auction.auctionId = :auctionId AND ar.registration = true")
    List<User> findAllByAuctionRegistration_Auction_AuctionId(@Param("auctionId") Integer auctionId);
}

