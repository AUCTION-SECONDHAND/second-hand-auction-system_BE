package com.second_hand_auction_system.repositories;
import com.second_hand_auction_system.models.Notifications;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationsRepository extends JpaRepository<Notifications, Integer> {
    List<Notifications> findAllByOrderByCreateAtDesc();
    List<Notifications> findByUser_IdOrderByCreateAtDesc(int id);
}
