package com.second_hand_auction_system.repositories;

import com.second_hand_auction_system.models.Order;
import com.second_hand_auction_system.utils.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    Page<Order> findAllByUser_Id( Integer userId, Pageable pageable);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

//    @Query("SELECT o FROM Order o WHERE o.status = :status")
//    Page<Order> findByStatus( OrderStatus status, Pageable pageable);


    boolean existsByAuction_AuctionIdAndUserId(Integer auctionId, Integer userId);


    Page<Order> findAllByAuction_Item_User_Id(Integer sellerId, Pageable pageable);


    @Query("SELECT " +
            "MONTH(o.createAt) AS month, " +
            "COUNT(o) AS totalOrders, " +
            "SUM(CASE WHEN o.status = 'delivered' THEN 1 ELSE 0 END) AS deliveredOrders, " +
            "SUM(CASE WHEN o.status = 'cancel' THEN 1 ELSE 0 END) AS cancelledOrders, " +
            "SUM(o.totalAmount) AS totalAmount " +
            "FROM Order o " +
            "GROUP BY MONTH(o.createAt) " +
            "ORDER BY MONTH(o.createAt)")
    List<Object[]> getOrderStatisticsByMonth();


    Order findByAuction_AuctionId(Integer auctionId);


    @Query("SELECT " +
            "FUNCTION('MONTH', o.createAt) AS month, " +
            "SUM(o.totalAmount) AS totalAmount " +
            "FROM Order o " +
            "GROUP BY FUNCTION('MONTH', o.createAt) " +
            "ORDER BY FUNCTION('MONTH', o.createAt) ASC")
    List<Object[]> getTotalMoneyByMonth();



}
