package com.second_hand_auction_system.repositories;

import com.second_hand_auction_system.models.SellerInformation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SellerInformationRepository extends JpaRepository<SellerInformation, Integer> {
    Optional<SellerInformation> findByUser_Id(Integer userId);

    @Query("SELECT si " +
            "FROM SellerInformation si " +
            "JOIN Item i ON i.user = si.user " +
            "JOIN FeedBack f ON f.item = i " +
            "GROUP BY si.sellerId " +
            "ORDER BY COUNT(f) DESC")
    Page<SellerInformation> findTop5SellersWithMostFeedback(Pageable pageable);

    @Query("SELECT si.sellerId FROM SellerInformation si WHERE si.user.id = :userId")
    Optional<Integer> findSellerIdByUserId(Integer userId);
}
