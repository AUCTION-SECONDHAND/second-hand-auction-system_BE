package com.second_hand_auction_system.repositories;

import com.second_hand_auction_system.models.ItemSpecific;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemSpecificRepository extends JpaRepository<ItemSpecific, Integer> {
    Optional<ItemSpecific> findByItem_ItemId(int itemId);
}
