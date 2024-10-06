package com.second_hand_auction_system.repositories;

import com.second_hand_auction_system.models.ImageItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageItemRepository extends JpaRepository<ImageItem, Integer> {
}
