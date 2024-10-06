package com.second_hand_auction_system.repositories;

import com.second_hand_auction_system.models.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Integer> {
    Item findByItemName(String itemName);
}
