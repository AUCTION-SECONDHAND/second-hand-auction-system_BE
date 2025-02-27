package com.second_hand_auction_system.converters.auctionRegistrations;

import com.second_hand_auction_system.dtos.responses.auction.ItemAuctionResponse;
import com.second_hand_auction_system.dtos.responses.auctionRegistrations.AuctionRegistrationsResponse;
import com.second_hand_auction_system.dtos.responses.item.AuctionItemResponse;
import com.second_hand_auction_system.dtos.responses.subCategory.SubCategoryItemResponse;
import com.second_hand_auction_system.models.*;
import org.springframework.stereotype.Component;

@Component
public class AuctionRegistrationsConverter {
    public AuctionRegistrationsResponse toAuctionRegistrationsResponse(AuctionRegistration auctionRegistration) {

        ItemAuctionResponse auctionResponse = null;
        if (auctionRegistration.getAuction() != null) {
            Auction auction = auctionRegistration.getAuction();
            auctionResponse = ItemAuctionResponse.builder()
                    .auctionId(auction.getAuctionId())
                    .startTime(auction.getStartTime())
                    .endTime(auction.getEndTime())
                    .startPrice(auction.getStartPrice())
                    .approveAt(auction.getApproveAt())
                    .createBy(auction.getCreateBy())
                    .buyNowPrice(auction.getBuyNowPrice())
                    .percentDeposit(auction.getPercentDeposit())
                    .startDate(auction.getStartDate())
                    .endDate(auction.getEndDate())
                    .status(auction.getStatus())
                    .build();
        }
        SubCategoryItemResponse subCategoryResponse = null;
        if (auctionRegistration.getAuction().getItem().getSubCategory() != null) {
            SubCategory subCategory = auctionRegistration.getAuction().getItem().getSubCategory();
            subCategoryResponse = SubCategoryItemResponse.builder()
                    .subCategoryId(subCategory.getSubCategoryId())
                    .subCategory(subCategory.getSubCategory())
                    .build();
        }

        AuctionItemResponse auctionItemResponse = null;
        if (auctionRegistration.getAuction().getItem() != null) {
            Item item = auctionRegistration.getAuction().getItem();
            auctionItemResponse = AuctionItemResponse.builder()
                    .itemId(item.getItemId())
                    .itemName(item.getItemName())
                    .itemDescription(item.getItemDescription())
                    .thumbnail(item.getThumbnail())
                    .itemStatus(item.getItemStatus())
                    .auction(auctionResponse)
                    .scId(subCategoryResponse)
                    .build();
        }

        return AuctionRegistrationsResponse.builder()
                .auctionRegistrationId(auctionRegistration.getAuctionRegistrationId())
                .depositeAmount(auctionRegistration.getDepositeAmount())
                .registration(auctionRegistration.getRegistration())
//                .note(auctionRegistration.getNote())
                .auctionItem(auctionItemResponse)
                .build();
    }

    public AuctionRegistrationsResponse toDetailedResponse(AuctionRegistration auctionRegistration) {
        User user = auctionRegistration.getUsers().isEmpty() ? null : auctionRegistration.getUsers().get(0);

        return AuctionRegistrationsResponse.builder()
                .auctionRegistrationId(auctionRegistration.getAuctionRegistrationId())
                .depositeAmount(auctionRegistration.getDepositeAmount())
                .registration(auctionRegistration.getRegistration())
                .createdDate(auctionRegistration.getCreateAt()) // Set createdDate from auctionRegistration
                .userName(user != null ? user.getFullName() : "Unknown") // Set user name
                .build();
    }



}
