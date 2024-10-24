package com.second_hand_auction_system.controller;

import com.second_hand_auction_system.dtos.request.order.OrderDTO;
import com.second_hand_auction_system.service.order.IOrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final IOrderService orderService;

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderDTO order, HttpServletRequest request) {
        return orderService.create(order,request);
    }

    @GetMapping
    public ResponseEntity<?> getAllOrders(@RequestParam(required = false) Integer page,
                                          @RequestParam(required = false) Integer size,
                                          @RequestParam(required = false) String sortBy) {
        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = "createAt";
        }
        return orderService.getOrders(page, size, sortBy);
    }

}
