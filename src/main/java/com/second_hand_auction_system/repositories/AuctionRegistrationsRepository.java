package com.second_hand_auction_system.repositories;

import com.second_hand_auction_system.models.AuctionRegistration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface AuctionRegistrationsRepository extends JpaRepository<AuctionRegistration, Integer> {

    Page<AuctionRegistration> findByUserId(@Param("userId") Integer userId, Pageable pageable);

}
