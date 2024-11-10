package com.second_hand_auction_system.controller;

import com.second_hand_auction_system.dtos.request.order.OrderDTO;
import com.second_hand_auction_system.service.order.IOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final IOrderService orderService;

    @PostMapping("")
    public ResponseEntity<?> createOrder(@RequestBody @Valid OrderDTO order) {
        return orderService.create(order);
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

    @GetMapping("/user")
    public ResponseEntity<?> getUserOrders(@RequestParam(value = "size",defaultValue = "10") int size,
                                           @RequestParam(value = "page",defaultValue = "0") int page) {
        return orderService.getOrderByUser(size,page);
    }

}
