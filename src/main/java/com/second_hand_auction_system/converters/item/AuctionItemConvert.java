package com.second_hand_auction_system.converters.item;

import com.second_hand_auction_system.dtos.responses.auction.ItemAuctionResponse;
import com.second_hand_auction_system.dtos.responses.auctionType.AuctionTypeResponse;
import com.second_hand_auction_system.dtos.responses.item.*;
import com.second_hand_auction_system.dtos.responses.subCategory.SubCategoryItemResponse;
import com.second_hand_auction_system.models.*;
import com.second_hand_auction_system.repositories.BidRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AuctionItemConvert {
    private final ModelMapper modelMapper;
    private final BidRepository bidRepository;

    public AuctionItemResponse toAuctionItemResponse(Item item) {
        // Ánh xạ Auction nếu tồn tại
        ItemAuctionResponse auctionResponse = null;
        if (item.getAuction() != null) {
            Auction auction = item.getAuction();

            Optional<Bid> bidOptional = bidRepository.findByAuction_AuctionIdAndWinBidTrue(auction.getAuctionId());
            Integer winBidAmount = null;
            if (bidOptional.isPresent()) {
                winBidAmount = bidOptional.get().getBidAmount();
            }

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
                    .percentDeposit(auction.getPercentDeposit())
                    .buyNowPrice(auction.getBuyNowPrice())
                    .winBid(winBidAmount)
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

        AuctionTypeResponse auctionTypeResponse = null;
        if (item.getAuctionType() != null) {
            AuctionType auctionType = item.getAuctionType();
            auctionTypeResponse = AuctionTypeResponse.builder()
                    .auctionTypeId(auctionType.getAuctionTypeId())
                    .auctionTypeName(auctionType.getAuctionTypeName())
                    .build();
        }


        ItemSpecificationResponse itemSpecificationResponse = null;
        if (item.getItemSpecification() != null) {
            ItemSpecification itemSpecification = item.getItemSpecification();
            itemSpecificationResponse = ItemSpecificationResponse.builder()
                    .ram(itemSpecification.getRam())
                    .itemSpecificationId(itemSpecification.getItemSpecificationId())
                    .sensors(itemSpecification.getSensors())
                    .screenSize(itemSpecification.getScreenSize())
                    .cameraSpecs(itemSpecification.getCameraSpecs())
                    .sim(itemSpecification.getSim())
                    .simSlots(itemSpecification.getSimSlots())
                    .os(itemSpecification.getOs())
                    .osFamily(itemSpecification.getOsFamily())
                    .bluetooth(itemSpecification.getBluetooth())
                    .usb(itemSpecification.getUsb())
                    .wlan(itemSpecification.getWlan())
                    .speed(itemSpecification.getSpeed())
                    .networkTechnology(itemSpecification.getNetworkTechnology())
                    .connectivity(itemSpecification.getConnectivity())
                    .build();
        }

        // Trả về AuctionItemResponse đã được ánh xạ
        return AuctionItemResponse.builder()
                .itemId(item.getItemId())
                .thumbnail(item.getThumbnail())
                .itemName(item.getItemName())
                .itemDescription(item.getItemDescription())
                .itemStatus(item.getItemStatus())
                .priceBuyNow(item.getPriceBuyNow())
                .auction(auctionResponse)
                .scId(subCategoryResponse)
                .auctionTypeId(auctionTypeResponse)
                .batteryHealth(item.getBatteryHealth())
                .osVersion(item.getOsVersion())
                .icloudStatus(item.getIcloudStatus())
                .bodyCondition(item.getBodyCondition())
                .screenCondition(item.getScreenCondition())
                .cameraCondition(item.getCameraCondition())
                .portCondition(item.getPortCondition())
                .buttonCondition(item.getButtonCondition())
                .itemSpecification(itemSpecificationResponse)
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
                    .percentDeposit(auction.getPercentDeposit())
                    .startDate(auction.getStartDate())
                    .endDate(auction.getEndDate())
                    .status(auction.getStatus())
                    .buyNowPrice(auction.getBuyNowPrice())
                    .build();
        }

        AuctionTypeResponse auctionTypeResponse = null;
        if (item.getAuctionType() != null) {
            AuctionType auctionType = item.getAuctionType();
            auctionTypeResponse = AuctionTypeResponse.builder()
                    .auctionTypeId(auctionType.getAuctionTypeId())
                    .auctionTypeName(auctionType.getAuctionTypeName())
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
        if(item.getItemSpecification() != null) {
            ItemSpecification itemSpecification = item.getItemSpecification();
            itemSpecificResponse = ItemSpecificResponse.builder()
                    .ram(itemSpecification.getRam())
                    .itemSpecificationId(itemSpecification.getItemSpecificationId())
                    .sensors(itemSpecification.getSensors())
                    .screenSize(itemSpecification.getScreenSize())
                    .cameraSpecs(itemSpecification.getCameraSpecs())
                    .sim(itemSpecification.getSim())
                    .simSlots(itemSpecification.getSimSlots())
                    .os(itemSpecification.getOs())
                    .osFamily(itemSpecification.getOsFamily())
                    .bluetooth(itemSpecification.getBluetooth())
                    .usb(itemSpecification.getUsb())
                    .wlan(itemSpecification.getWlan())
                    .speed(itemSpecification.getSpeed())
                    .networkTechnology(itemSpecification.getNetworkTechnology())
                    .connectivity(itemSpecification.getConnectivity())
                    .build();
        }

        // Trả về ItemDetailResponse đã được ánh xạ
        return ItemDetailResponse.builder()
                .itemId(item.getItemId())
                .thumbnail(item.getThumbnail())
                .itemName(item.getItemName())
                .itemDescription(item.getItemDescription())
                .itemStatus(item.getItemStatus())
                .priceBuyNow(item.getPriceBuyNow())
                .reason(item.getReason())
                .auction(auctionResponse)
                .imei(item.getImei())
                .color(item.getColor())
                .batteryHealth(item.getBatteryHealth())
                .bodyCondition(item.getBodyCondition())
                .buttonCondition(item.getButtonCondition())
                .cameraCondition(item.getCameraCondition())
                .osVersion(item.getOsVersion())
                .portCondition(item.getPortCondition())
                .screenCondition(item.getScreenCondition())
                .storage(item.getStorage())
                .icloudStatus(item.getIcloudStatus())
                .priceBuyNow(item.getPriceBuyNow())
                .scId(subCategoryResponse)
                .itemSpecific(itemSpecificResponse)
                .images(imageResponses)
                .itemDocument(item.getItemDocument())
                .priceStepItem(item.getPriceStepItem())
                .auctionType(auctionTypeResponse)
                .numberParticipant(0)
                .brand(item.getBrand())
                .model(item.getModel())
                .serial(item.getSerial())
                .controlNumber(item.getControlNumber())
                .valid(item.getValid())
                .manufacturer(item.getManufacturer())
                .deviceImage(item.getDeviceImage())
                .build();
    }


    public ItemResponse toItemResponse(Item item) {
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

        AuctionTypeResponse auctionTypeResponse = null;
        if (item.getAuctionType() != null) {
            AuctionType auctionType = item.getAuctionType();
            auctionTypeResponse = AuctionTypeResponse.builder()
                    .auctionTypeId(auctionType.getAuctionTypeId())
                    .auctionTypeName(auctionType.getAuctionTypeName())
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

        List<ImageItemResponse> imageResponses = item.getImageItems().stream()
                .map(image -> ImageItemResponse.builder()
                        .idImage(image.getImageItemId())
                        .image(image.getImageUrl())
                        .build())
                .collect(Collectors.toList());

        return ItemResponse.builder()
                .itemId(item.getItemId())
                .thumbnail(item.getThumbnail())
                .itemName(item.getItemName())
                .itemDescription(item.getItemDescription())
                .itemStatus(item.getItemStatus())
                .priceBuyNow(item.getPriceBuyNow())
                .auction(auctionResponse)
                .auctionTypeResponse(auctionTypeResponse)
                .scId(subCategoryResponse)
                .imageItemResponse(imageResponses)
                .itemDocument(item.getItemDocument())
                .priceStepItem(item.getPriceStepItem())
                .createBy(item.getCreateBy())
                .createAt(String.valueOf(item.getCreateAt()))
                .updateAt(String.valueOf(item.getUpdateAt()))
                .build();
    }
}
