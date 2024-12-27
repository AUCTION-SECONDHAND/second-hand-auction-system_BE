package com.second_hand_auction_system.repositories;

import com.second_hand_auction_system.models.ItemSpecification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemSpecificationRepository extends JpaRepository<ItemSpecification, Integer> {
}
