package com.second_hand_auction_system.converters.sellerInformation;

import com.second_hand_auction_system.dtos.request.sellerInfomation.SellerInformationDto;
import com.second_hand_auction_system.dtos.responses.sellerInformation.SellerInformationResponse;
import com.second_hand_auction_system.models.SellerInformation;
import com.second_hand_auction_system.models.User;

public class SellerInformationConverter {

    public static SellerInformation convertToEntity(SellerInformationDto sellerInformationDto, User user) {
        return SellerInformation.builder()
                .storeName(sellerInformationDto.getStoreName())
                .address(sellerInformationDto.getAddress())
                .description(sellerInformationDto.getDescription())
                .avatar(sellerInformationDto.getAvatar())
                .backgroundImage(sellerInformationDto.getBackgroundImage())
                .user(user) // Liên kết với user
                .build();
    }

    public static SellerInformationResponse convertToResponse(SellerInformation sellerInformation) {
        return SellerInformationResponse.builder()
                .sellerId(sellerInformation.getSellerId())
                .storeName(sellerInformation.getStoreName())
                .address(sellerInformation.getAddress())
                .description(sellerInformation.getDescription())
                .avatar(sellerInformation.getAvatar())
                .backgroundImage(sellerInformation.getBackgroundImage())
                .userId(sellerInformation.getUser().getId())
                .build();
    }
}