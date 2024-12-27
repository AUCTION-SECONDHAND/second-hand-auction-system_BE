package com.second_hand_auction_system.dtos.responses.order;

import com.second_hand_auction_system.dtos.responses.auction.AuctionResponse;
import com.second_hand_auction_system.dtos.responses.item.ItemDetailResponse;
import com.second_hand_auction_system.models.Auction;
import com.second_hand_auction_system.models.Item;
import com.second_hand_auction_system.models.User;
import com.second_hand_auction_system.utils.OrderStatus;
import com.second_hand_auction_system.utils.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class OrderDetailResponse {
    private Integer orderId;

    private double totalAmount;

    private String email;

    private String fullName;

    private String phoneNumber;

    //@Enumerated(EnumType.STRING)
    private OrderStatus status;

    //@Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private String shippingMethod;

    private String note;

    private String address;

    private String createBy;

    private ItemDetailResponse item;

    private String orderCode;

    //private AuctionResponse auction;

    //private User user;
}
