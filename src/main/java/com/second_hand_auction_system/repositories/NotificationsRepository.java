package com.second_hand_auction_system.repositories;

import com.second_hand_auction_system.models.Notifications;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationsRepository extends JpaRepository<Notifications, Integer> {
}
