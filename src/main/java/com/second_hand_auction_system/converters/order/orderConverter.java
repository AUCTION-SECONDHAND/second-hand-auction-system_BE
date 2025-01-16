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
                    .images(imageResponses)
                    .build();
        }


        ItemSpecificResponse itemSpecificResponse = null;

        if (order.getItem() != null) {
            Item item = order.getItem();

            // Map dữ liệu từ item.getItemSpecification()
            itemSpecificResponse = ItemSpecificResponse.builder()
                    .itemSpecificationId(item.getItemSpecification().getItemSpecificationId())
                    .ram(item.getItemSpecification().getRam())
                    .screenSize(item.getItemSpecification().getScreenSize())
                    .cameraSpecs(item.getItemSpecification().getCameraSpecs())
                    .connectivity(item.getItemSpecification().getConnectivity())
                    .sensors(item.getItemSpecification().getSensors())
                    .sim(item.getItemSpecification().getSim())
                    .simSlots(item.getItemSpecification().getSimSlots())
                    .os(item.getItemSpecification().getOs())
                    .osFamily(item.getItemSpecification().getOsFamily())
                    .bluetooth(item.getItemSpecification().getBluetooth())
                    .usb(item.getItemSpecification().getUsb())
                    .wlan(item.getItemSpecification().getWlan())
                    .speed(item.getItemSpecification().getSpeed())
                    .networkTechnology(item.getItemSpecification().getNetworkTechnology())
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
                .orderCode(order.getOrderCode())
                .build();

    }


}
