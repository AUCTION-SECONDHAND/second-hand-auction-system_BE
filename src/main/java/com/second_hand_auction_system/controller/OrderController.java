package com.second_hand_auction_system.controller;

import com.second_hand_auction_system.dtos.request.order.OrderDTO;
import com.second_hand_auction_system.dtos.responses.ResponseObject;
import com.second_hand_auction_system.dtos.responses.order.OrderDetailResponse;
import com.second_hand_auction_system.service.auction.AuctionService;
import com.second_hand_auction_system.service.order.IOrderService;
import com.second_hand_auction_system.utils.OrderStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final IOrderService orderService;
    private final AuctionService auctionService;

    @PostMapping("")
    public ResponseEntity<?> createOrder(@RequestBody @Valid OrderDTO order) {
        return orderService.create(order);
    }

    @PatchMapping("/{orderId}")
    public ResponseEntity<?> updateOrder() {
        orderService.updateOrderStatuses();
        return ResponseEntity.ok(
                ResponseObject.builder()
                        .status(HttpStatus.OK)
                        .message("Success")
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<?> getAllOrders(@RequestParam(required = false) Integer page,
                                          @RequestParam(required = false) Integer size,
                                          @RequestParam(required = false) String sortBy,
                                          @RequestParam(required = false) OrderStatus status) {
        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = "createAt";
        }
        return orderService.getOrders(page, size, sortBy, status);
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUserOrders(@RequestParam(value = "size", defaultValue = "10") int size,
                                           @RequestParam(value = "page", defaultValue = "0") int page) {
        return orderService.getOrderByUser(size, page);
    }

    @GetMapping("/revenue")
    public ResponseEntity<?> getStatistics() {
        return orderService.getStatistic();
    }

    @GetMapping("/seller")
    public ResponseEntity<?> getSellerOrders(@RequestParam(value = "size", defaultValue = "10") int size,
                                             @RequestParam(value = "page", defaultValue = "0") int page) {
        return orderService.getOrderBySeller(size, page);
    }

    @GetMapping("/detail/{orderId}")
    public ResponseEntity<?> getOrderDetails(@PathVariable int orderId) {
        OrderDetailResponse orderDetailResponse = orderService.getOrderDetail(orderId);
        return ResponseEntity.ok(
                ResponseObject.builder()
                        .status(HttpStatus.OK)
                        .message("Success")
                        .data(orderDetailResponse)
                        .build()
        );
    }

    @GetMapping("/statistics/monthly")
    public ResponseEntity<?> getOrderStatisticsByMonth() {
        return orderService.getOrderStatisticsByMonth();
    }

    @GetMapping("/getTotalMoneyByMonth")
    public ResponseEntity<?> getTotalMoneyByMonth() {
        return orderService.getTotalMoney();
    }

}
