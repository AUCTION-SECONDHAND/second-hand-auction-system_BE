package com.second_hand_auction_system.dtos.request.sellerInfomation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SellerInformationDto {
    private String storeName;
    private String address;
    private String description;
    private String avatar;
    private String backgroundImage;
    private Integer userId;
}