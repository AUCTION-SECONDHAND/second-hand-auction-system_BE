package com.second_hand_auction_system.dtos.responses.order;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
public class SellerOrderStatics {
    private int month;
    private long totalOrders;
    private long deliveredOrders;
    private long cancelledOrders;
    private double totalAmount;
}
