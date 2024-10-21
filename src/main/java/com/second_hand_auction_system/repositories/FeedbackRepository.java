package com.second_hand_auction_system.repositories;

import com.second_hand_auction_system.models.FeedBack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<FeedBack, Integer> {
    List<FeedBack> findAllByItem_ItemId(Integer itemId);

    List<FeedBack> findAllByUser_Id(Integer userId);
}
