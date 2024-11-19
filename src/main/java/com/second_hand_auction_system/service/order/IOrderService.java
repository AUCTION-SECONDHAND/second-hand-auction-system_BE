package com.second_hand_auction_system.service.order;

import com.second_hand_auction_system.dtos.request.order.OrderDTO;
import com.second_hand_auction_system.dtos.responses.order.OrderDetailResponse;
import com.second_hand_auction_system.utils.OrderStatus;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

public interface IOrderService {
    ResponseEntity<?> create(OrderDTO order);

    ResponseEntity<?> getOrders(Integer page, Integer pageSize, String sortBy, OrderStatus status);

    ResponseEntity<?> getOrderByUser(int size, int page);

    ResponseEntity<?> getStatistic();

    ResponseEntity<?> getOrderBySeller(int size, int page);

    void updateOrderStatuses();

    OrderDetailResponse getOrderDetail(int orderId);
}
