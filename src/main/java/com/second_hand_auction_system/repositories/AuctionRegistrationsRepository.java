package com.second_hand_auction_system.repositories;

import com.second_hand_auction_system.models.AuctionRegistration;
import com.second_hand_auction_system.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AuctionRegistrationsRepository extends JpaRepository<AuctionRegistration, Integer> {

    Page<AuctionRegistration> findByUserId(@Param("userId") Integer userId, Pageable pageable);
    List<AuctionRegistration> findByUserId(Integer user_id);
    AuctionRegistration findByUserIdAndAuction_AuctionId(Integer user_id, Integer auction_auctionId);

}
