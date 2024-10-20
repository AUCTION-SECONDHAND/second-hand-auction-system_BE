package com.second_hand_auction_system.repositories;

import com.second_hand_auction_system.models.Item;
import com.second_hand_auction_system.utils.ItemStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Integer> {
    Page<Item> findAllByItemStatus(ItemStatus itemStatus, Pageable pageable);
}
