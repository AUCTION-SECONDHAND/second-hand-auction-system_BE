package com.second_hand_auction_system.service.order;

import com.second_hand_auction_system.dtos.request.order.OrderDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

public interface IOrderService {
    ResponseEntity<?> create(OrderDTO order, HttpServletRequest request);

    ResponseEntity<?> getOrders(Integer page,Integer pageSize,String sortBy);

    ResponseEntity<?> getOrderByUser(int size, int page);
}
