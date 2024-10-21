package com.second_hand_auction_system.dtos.responses.sellerInformation;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class SellerInformationResponse {
    private Integer sellerId;
    private String storeName;
    private String address;
    private String description;
    private String avatar;
    private String backgroundImage;
    private Integer userId;
}