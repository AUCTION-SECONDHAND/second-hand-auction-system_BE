package com.second_hand_auction_system.repositories;

import com.second_hand_auction_system.models.Auction;
import com.second_hand_auction_system.utils.AuctionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.Time;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Integer> {
    List<Auction> findAllByEndTimeBeforeAndStatus(Time endTime, AuctionStatus status);

    @Query("SELECT MAX(b.bidAmount) FROM Bid b WHERE b.auction.auctionId = :auctionId")
    Double findMaxBidByAuctionId(Integer auctionId);

    List<Auction> findAllByEndDateBeforeOrEndDateEqualsAndEndTimeBeforeAndStatus(Date currentDate, Date currentDate1, Time currentTime, AuctionStatus auctionStatus);
}
