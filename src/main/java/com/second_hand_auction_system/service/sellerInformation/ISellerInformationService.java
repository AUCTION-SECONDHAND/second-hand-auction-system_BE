package com.second_hand_auction_system.service.sellerInformation;

import com.second_hand_auction_system.dtos.request.sellerInfomation.SellerInformationDto;
import com.second_hand_auction_system.dtos.responses.sellerInformation.SellerInformationResponse;

public interface ISellerInformationService {
    SellerInformationResponse createSellerInformation(SellerInformationDto sellerInformationDto, Integer userId) throws Exception;

    SellerInformationResponse updateSellerInformation(Integer sellerId, SellerInformationDto sellerInformationDto) throws Exception;

    SellerInformationResponse getSellerInformationById(Integer sellerId) throws Exception;

    void deleteSellerInformation(Integer sellerId) throws Exception;

    SellerInformationResponse getSellerInformationByUserId(Integer userId) throws Exception;
}
