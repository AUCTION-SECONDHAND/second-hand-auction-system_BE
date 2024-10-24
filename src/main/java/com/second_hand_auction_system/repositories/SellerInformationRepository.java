package com.second_hand_auction_system.repositories;

import com.second_hand_auction_system.models.SellerInformation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SellerInformationRepository extends JpaRepository<SellerInformation, Integer> {
    Optional<SellerInformation> findByUser_Id(Integer userId);
}
