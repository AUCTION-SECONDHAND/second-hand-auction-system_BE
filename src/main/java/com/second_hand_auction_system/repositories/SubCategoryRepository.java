package com.second_hand_auction_system.repositories;

import com.second_hand_auction_system.models.SubCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubCategoryRepository extends JpaRepository<SubCategory, Integer> {
    SubCategory findBySubCategory(String name);
}
