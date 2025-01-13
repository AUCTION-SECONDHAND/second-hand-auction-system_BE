package com.second_hand_auction_system.repositories;

import com.second_hand_auction_system.models.Auction;
import com.second_hand_auction_system.models.Bid;
import com.second_hand_auction_system.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface BidRepository extends JpaRepository<Bid, Integer> {

    List<Bid> findByAuction_AuctionId(Integer auctionId);

    Optional<Bid> findTopByAuction_AuctionIdAndBidAmount(Integer auctionId, Integer bid);

    List<Bid> findByAuction_AuctionIdOrderByBidAmountDesc(Integer auctionId);

    Page<Bid> findAllByAuction_AuctionIdOrderByBidAmountDesc(Integer auctionId, Pageable pageable);

    Optional<Bid> findByUserAndAuction(User user, Auction auction);

    long countByAuction_AuctionId(Integer auctionId);

    Collection<Object> findAllByAuction_AuctionId(Integer auctionId);

    @Query("SELECT COUNT(DISTINCT b.user) FROM Bid b WHERE b.auction.auctionId = :auctionId")
    int countDistinctUsersByAuctionId(Integer auctionId);

    @Query("SELECT COUNT(b) FROM Bid b WHERE b.auction.auctionId = :auctionId AND b.bidAmount > 0")
    long countBidsByAuctionId(@Param("auctionId") Integer auctionId);

    Bid findTopByAuction_AuctionIdAndWinBidTrue(Integer auctionId);

    Optional<Bid> findByAuction_AuctionIdAndUser_Id(Integer auctionId, Integer userId);

    boolean existsByUserIdAndAuction_AuctionId(Integer userId, Integer auctionId);

    Optional<Bid> findByUserIdAndAuction_AuctionId(Integer userId, Integer auctionId);

    List<Bid> findByAuction_AuctionIdAndWinBidFalse(Integer auctionId);

    Optional<Bid> findByAuction_AuctionIdAndWinBidTrue(Integer auctionId);

    Bid findTopByAuction_AuctionIdOrderByBidAmountDesc(Integer auctionId);

    @Query("SELECT DISTINCT b.user FROM Bid b WHERE b.auction.auctionId = :auctionId")
    List<User> findDistinctUsersByAuction_AuctionId(Integer auctionId);

    @Query("SELECT b.user FROM Bid b WHERE b.auction.auctionId = :auctionId AND b.winBid = false")
    List<User> findLosersByAuctionId(@Param("auctionId") Integer auctionId);

}

