package com.second_hand_auction_system.converters.order;

import com.second_hand_auction_system.dtos.responses.auction.ItemAuctionResponse;
import com.second_hand_auction_system.dtos.responses.item.ImageItemResponse;
import com.second_hand_auction_system.dtos.responses.item.ItemDetailResponse;
import com.second_hand_auction_system.dtos.responses.item.ItemSpecificResponse;
import com.second_hand_auction_system.dtos.responses.order.OrderDetailResponse;
import com.second_hand_auction_system.dtos.responses.subCategory.SubCategoryItemResponse;
import com.second_hand_auction_system.models.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class orderConverter {
    public OrderDetailResponse toOrderDetailResponse(Order order) {
        List<ImageItemResponse> imageResponses = order.getItem().getImageItems().stream()
                .map(image -> ImageItemResponse.builder()
                        .idImage(image.getImageItemId())
                        .image(image.getImageUrl())
                        .build())
                .collect(Collectors.toList());

        ItemAuctionResponse auctionResponse = null;
        if (order.getItem().getAuction() != null) {
            Auction auction = order.getItem().getAuction();
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
        if (order.getItem().getSubCategory() != null) {
            SubCategory subCategory = order.getItem().getSubCategory();
            subCategoryResponse = SubCategoryItemResponse.builder()
                    .subCategoryId(subCategory.getSubCategoryId())
                    .subCategory(subCategory.getSubCategory())
                    .build();
        }

        ItemSpecificResponse itemSpecificResponse = null;
        if (order.getItem().getItemSpecific() != null) {
            ItemSpecific itemSpecific = order.getItem().getItemSpecific();
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

        ItemDetailResponse itemDetailResponse = null;
        if (order.getItem() != null) {
            Item item = order.getItem();
            itemDetailResponse = ItemDetailResponse.builder()
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

        return OrderDetailResponse.builder()
                .orderId(order.getOrderId())
                .totalAmount(order.getTotalAmount())
                .email(order.getEmail())
                .fullName(order.getFullName())
                .phoneNumber(order.getPhoneNumber())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .shippingMethod(order.getShippingMethod())
                .note(order.getNote())
                .address(order.getAddress())
                .item(itemDetailResponse)
                .build();
    }
}
