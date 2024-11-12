package com.second_hand_auction_system.repositories;

import com.second_hand_auction_system.models.Order;
import com.second_hand_auction_system.utils.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    Page<Order> findAllByUser_Id( Integer userId, Pageable pageable);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
}
