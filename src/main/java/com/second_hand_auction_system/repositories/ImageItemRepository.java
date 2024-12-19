package com.second_hand_auction_system.repositories;

import com.second_hand_auction_system.models.ImageItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageItemRepository extends JpaRepository<ImageItem, Integer> {
    List<ImageItem> findByItem_ItemId(Integer itemItemId);
}
