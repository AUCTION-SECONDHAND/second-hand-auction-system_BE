package com.second_hand_auction_system.repositories;

import com.second_hand_auction_system.models.Auction;
import com.second_hand_auction_system.models.Bid;
import com.second_hand_auction_system.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BidRepository extends JpaRepository<Bid, Integer> {

    List<Bid> findByAuction_AuctionId(Integer auctionId);

    Optional<Bid> findTopByAuction_AuctionIdAndBidAmount(Integer auctionId, Integer bid);

    List<Bid> findByAuction_AuctionIdOrderByBidAmountDesc(Integer auctionId);

    Page<Bid> findAllByAuction_AuctionIdOrderByBidAmountDesc(Integer auctionId, Pageable pageable);

    Optional<Bid> findByUserAndAuction(User user, Auction auction);

}