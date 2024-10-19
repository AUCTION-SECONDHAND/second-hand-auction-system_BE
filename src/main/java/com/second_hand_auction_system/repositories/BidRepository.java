package com.second_hand_auction_system.repositories;

import com.second_hand_auction_system.models.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BidRepository extends JpaRepository<Bid, Integer> {

    List<Bid> findByAuction_AuctionId(Integer auctionId);
}