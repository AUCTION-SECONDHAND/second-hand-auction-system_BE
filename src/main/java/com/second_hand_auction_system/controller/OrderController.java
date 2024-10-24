package com.second_hand_auction_system.controller;

import com.second_hand_auction_system.dtos.request.order.OrderDTO;
import com.second_hand_auction_system.service.order.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final IOrderService orderService;

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderDTO order) {
        return orderService.create(order);
    }

    @GetMapping
    public ResponseEntity<?> getAllOrders(@RequestParam(required = false) String search,
                                          @RequestParam(required = false) Integer page,
                                          @RequestParam(required = false) Integer size) {
        {
            return orderService.getOrders(search, page, size);
        }

    }
}
