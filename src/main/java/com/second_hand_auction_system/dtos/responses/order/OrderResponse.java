package com.second_hand_auction_system.dtos.responses.order;

import com.second_hand_auction_system.dtos.responses.auction.AuctionOrder;
import com.second_hand_auction_system.dtos.responses.auction.AuctionResponse;
import com.second_hand_auction_system.dtos.responses.item.ItemBriefResponseOrder;
import com.second_hand_auction_system.utils.OrderStatus;
import com.second_hand_auction_system.utils.PaymentMethod;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class OrderResponse {
    private Integer orderId;
    private OrderStatus orderStatus;
    private PaymentMethod paymentMethod;
    private String email;
    private String phoneNumber;
    private int quantity;
    private String note;
    private ItemBriefResponseOrder item;        // Thông tin sản phẩm
    private AuctionOrder auctionOrder;
    private String createBy;
    private double totalPrice;
    private String shippingType;


}
