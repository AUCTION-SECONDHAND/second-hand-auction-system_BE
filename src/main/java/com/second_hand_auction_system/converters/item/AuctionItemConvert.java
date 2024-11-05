package com.second_hand_auction_system.converters.item;

import com.second_hand_auction_system.dtos.responses.auction.ItemAuctionResponse;
import com.second_hand_auction_system.dtos.responses.item.AuctionItemResponse;
import com.second_hand_auction_system.dtos.responses.item.ImageItemResponse;
import com.second_hand_auction_system.dtos.responses.item.ItemDetailResponse;
import com.second_hand_auction_system.dtos.responses.item.ItemSpecificResponse;
import com.second_hand_auction_system.dtos.responses.subCategory.SubCategoryItemResponse;
import com.second_hand_auction_system.models.Auction;
import com.second_hand_auction_system.models.Item;
import com.second_hand_auction_system.models.ItemSpecific;
import com.second_hand_auction_system.models.SubCategory;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

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

        // Ánh xạ danh mục phụ nếu tồn tại
        SubCategoryItemResponse subCategoryResponse = null;
        if (item.getSubCategory() != null) {
            SubCategory subCategory = item.getSubCategory();
            subCategoryResponse = SubCategoryItemResponse.builder()
                    .subCategoryId(subCategory.getSubCategoryId())
                    .subCategory(subCategory.getSubCategory())
                    .build();
        }

        // Trả về AuctionItemResponse đã được ánh xạ
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

    public ItemDetailResponse toAuctionDetailItemResponse(Item item) {
        // Ánh xạ danh sách ảnh
        List<ImageItemResponse> imageResponses = item.getImageItems().stream()
                .map(image -> ImageItemResponse.builder()
                        .idImage(image.getImageItemId())
                        .image(image.getImageUrl())
                        .build())
                .collect(Collectors.toList());

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

        // Ánh xạ danh mục phụ nếu tồn tại
        SubCategoryItemResponse subCategoryResponse = null;
        if (item.getSubCategory() != null) {
            SubCategory subCategory = item.getSubCategory();
            subCategoryResponse = SubCategoryItemResponse.builder()
                    .subCategoryId(subCategory.getSubCategoryId())
                    .subCategory(subCategory.getSubCategory())
                    .build();
        }

        // Ánh xạ các thông tin cụ thể của item nếu có
        ItemSpecificResponse itemSpecificResponse = null;
        if (item.getItemSpecific() != null) {
            ItemSpecific itemSpecific = item.getItemSpecific();
            itemSpecificResponse = ItemSpecificResponse.builder()
                    .percent(itemSpecific.getPercent())
                    .type(itemSpecific.getType())
                    .color(itemSpecific.getColor())
                    .weight(itemSpecific.getWeight())
                    .dimension(itemSpecific.getDimension())
                    .original(itemSpecific.getOriginal())
                    .manufactureDate(itemSpecific.getManufactureDate())
                    .material(itemSpecific.getMaterial())
                    .build();
        }

        // Trả về ItemDetailResponse đã được ánh xạ
        return ItemDetailResponse.builder()
                .itemId(item.getItemId())
                .thumbnail(item.getThumbnail())
                .itemName(item.getItemName())
                .itemDescription(item.getItemDescription())
                .itemStatus(item.getItemStatus())
                .auction(auctionResponse)
                .scId(subCategoryResponse)
                .itemSpecific(itemSpecificResponse)
                .images(imageResponses)
                .build();
    }
}
