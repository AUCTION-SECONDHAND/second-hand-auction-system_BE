package com.second_hand_auction_system.repositories;

import com.second_hand_auction_system.models.MainCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MainCategoryRepository extends JpaRepository<MainCategory, Integer> {
    MainCategory findByCategoryName (String categoryName);
    MainCategory findByMainCategoryId (int mainCategoryId);
}
