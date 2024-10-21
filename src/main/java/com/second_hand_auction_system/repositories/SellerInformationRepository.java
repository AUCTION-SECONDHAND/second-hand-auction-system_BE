package com.second_hand_auction_system.repositories;

import com.second_hand_auction_system.models.SellerInformation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerInformationRepository extends JpaRepository<SellerInformation, Integer> {
}
