package com.second_hand_auction_system.repositories;

import com.second_hand_auction_system.models.WithdrawRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WithdrawRequestRepository extends JpaRepository<WithdrawRequest, Integer> {
}
