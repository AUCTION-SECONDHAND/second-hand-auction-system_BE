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
            ItemSpecification itemSpecification = order.getItem().getItemSpecification();

            if (itemSpecification != null) { // Kiểm tra null cho itemSpecification
                // Map dữ liệu từ itemSpecification
                itemSpecificResponse = ItemSpecificResponse.builder()
                        .itemSpecificationId(itemSpecification.getItemSpecificationId())
                        .ram(itemSpecification.getRam())
                        .screenSize(itemSpecification.getScreenSize())
                        .cameraSpecs(itemSpecification.getCameraSpecs())
                        .connectivity(itemSpecification.getConnectivity())
                        .sensors(itemSpecification.getSensors())
                        .sim(itemSpecification.getSim())
                        .simSlots(itemSpecification.getSimSlots())
                        .os(itemSpecification.getOs())
                        .osFamily(itemSpecification.getOsFamily())
                        .bluetooth(itemSpecification.getBluetooth())
                        .usb(itemSpecification.getUsb())
                        .wlan(itemSpecification.getWlan())
                        .speed(itemSpecification.getSpeed())
                        .networkTechnology(itemSpecification.getNetworkTechnology())
                        .build();
            }
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
