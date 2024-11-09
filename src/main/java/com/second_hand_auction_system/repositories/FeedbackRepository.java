package com.second_hand_auction_system.repositories;

import com.second_hand_auction_system.models.FeedBack;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<FeedBack, Integer> {
    List<FeedBack> findAllByItem_ItemId(Integer itemId);

    List<FeedBack> findAllByUser_Id(Integer userId);

    @Query("SELECT f FROM FeedBack f WHERE f.item.user.id = :userId AND f.item.user.role = 'SELLER'")
    Page<FeedBack> findAllBySellerUserId(@Param("userId") Integer userId, Pageable pageable);



}
