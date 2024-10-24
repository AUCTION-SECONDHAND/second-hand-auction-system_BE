package com.second_hand_auction_system.dtos.responses.order;

import com.second_hand_auction_system.utils.OrderStatus;
import com.second_hand_auction_system.utils.PaymentMethod;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class OrderResponse {
//    private int orderId;
    private OrderStatus orderStatus;
    private PaymentMethod paymentMethod;
    private String email;
    private String phoneNumber;
    private int quantity;
    private String note;
    private Integer itemId;
    private Integer auctionId;
    private String createBy;
    private double totalPrice;
    private String shippingType;
}
