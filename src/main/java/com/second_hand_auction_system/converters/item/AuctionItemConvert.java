package com.second_hand_auction_system.converters.item;

import com.second_hand_auction_system.dtos.responses.auction.ItemAuctionResponse;
import com.second_hand_auction_system.dtos.responses.item.AuctionItemResponse;
import com.second_hand_auction_system.dtos.responses.subCategory.SubCategoryItemResponse;
import com.second_hand_auction_system.models.Auction;
import com.second_hand_auction_system.models.Item;
import com.second_hand_auction_system.models.SubCategory;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuctionItemConvert {
    private final ModelMapper modelMapper;

    public AuctionItemResponse toAuctionItemResponse(Item item) {
        // Ánh xạ Auction nếu tồn tại
        ItemAuctionResponse auctionResponse = null;
        if (item.getAuction() != null) {
            Auction auction = item.getAuction();
            auctionResponse = ItemAuctionResponse.builder()
                    .auctionId(auction.getAuctionId())
                    .startTime(auction.getStartTime())
                    .endTime(auction.getEndTime())
                    .startPrice(auction.getStartPrice())
                    .approveAt(auction.getApproveAt())
                    .createBy(auction.getCreateBy())
                    .startDate(auction.getStartDate())
                    .endDate(auction.getEndDate())
                    .status(auction.getStatus())
                    .build();
        }
        SubCategoryItemResponse subCategoryResponse = null;
        if (item.getSubCategory() != null) {
            SubCategory subCategory = item.getSubCategory();
            subCategoryResponse = SubCategoryItemResponse.builder()
                    .subCategoryId(subCategory.getSubCategoryId())
                    .subCategory(subCategory.getSubCategory())
                    .build();
        }

        // Sử dụng builder pattern để tạo AuctionItemResponse
        return AuctionItemResponse.builder()
                .itemId(item.getItemId())
                .thumbnail(item.getThumbnail())
                .itemName(item.getItemName())
                .itemDescription(item.getItemDescription())
                .itemStatus(item.getItemStatus())
                .auction(auctionResponse)
                .scId(subCategoryResponse)
                .build();
    }
}
