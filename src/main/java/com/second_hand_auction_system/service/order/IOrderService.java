package com.second_hand_auction_system.service.order;

import com.second_hand_auction_system.dtos.request.order.OrderDTO;
import org.springframework.http.ResponseEntity;

public interface IOrderService {
    ResponseEntity<?> create(OrderDTO order);

    ResponseEntity<?> getOrders(String search ,Integer page,Integer pageSize);
}
