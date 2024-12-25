package com.second_hand_auction_system.converters.item;

import com.second_hand_auction_system.dtos.responses.auction.ItemAuctionResponse;
import com.second_hand_auction_system.dtos.responses.auctionType.AuctionTypeResponse;
import com.second_hand_auction_system.dtos.responses.item.AuctionItemResponse;
import com.second_hand_auction_system.dtos.responses.item.ImageItemResponse;
import com.second_hand_auction_system.dtos.responses.item.ItemDetailResponse;
import com.second_hand_auction_system.dtos.responses.item.ItemResponse;
import com.second_hand_auction_system.dtos.responses.subCategory.SubCategoryItemResponse;
import com.second_hand_auction_system.models.Auction;
import com.second_hand_auction_system.models.Bid;
import com.second_hand_auction_system.models.Item;
import com.second_hand_auction_system.repositories.BidRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AuctionItemConvert {
    private final ModelMapper modelMapper;
    private final BidRepository bidRepository;

    // Phương thức chuyển Item sang AuctionItemResponse hỗ trợ nhiều phiên đấu giá
    public AuctionItemResponse toAuctionItemResponse(Item item) {
        // Ánh xạ danh sách Auction
        List<ItemAuctionResponse> auctionResponses = item.getAuctions().stream()
                .map(auction -> {
                    Optional<Bid> bidOptional = bidRepository.findByAuction_AuctionIdAndWinBidTrue(auction.getAuctionId());
                    Integer winBidAmount = bidOptional.map(Bid::getBidAmount).orElse(null);

                    return ItemAuctionResponse.builder()
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
                })
                .collect(Collectors.toList());

        // Ánh xạ danh mục phụ nếu tồn tại
        SubCategoryItemResponse subCategoryResponse = item.getSubCategory() != null ?
                SubCategoryItemResponse.builder()
                        .subCategoryId(item.getSubCategory().getSubCategoryId())
                        .subCategory(item.getSubCategory().getSubCategory())
                        .build() : null;

        // Ánh xạ loại đấu giá nếu tồn tại
        AuctionTypeResponse auctionTypeResponse = item.getAuctionType() != null ?
                AuctionTypeResponse.builder()
                        .auctionTypeId(item.getAuctionType().getAuctionTypeId())
                        .auctionTypeName(item.getAuctionType().getAuctionTypeName())
                        .build() : null;

        // Trả về AuctionItemResponse đã được ánh xạ
        return AuctionItemResponse.builder()
                .itemId(item.getItemId())
                .thumbnail(item.getThumbnail())
                .itemName(item.getItemName())
                .itemDescription(item.getItemDescription())
                .itemStatus(item.getItemStatus())
                .priceBuyNow(item.getPriceBuyNow())
                .auction(auctionResponses) // Ánh xạ danh sách các phiên đấu giá
                .scId(subCategoryResponse)
                .auctionTypeId(auctionTypeResponse)
                .build();
    }

    // Phương thức chi tiết Item, bao gồm thông tin về các ảnh, Auction, loại đấu giá, danh mục
    public ItemDetailResponse toAuctionDetailItemResponse(Item item) {
        // Ánh xạ danh sách ảnh
        List<ImageItemResponse> imageResponses = item.getImageItems().stream()
                .map(image -> ImageItemResponse.builder()
                        .idImage(image.getImageItemId())
                        .image(image.getImageUrl())
                        .build())
                .collect(Collectors.toList());

        // Ánh xạ danh sách Auction
        List<ItemAuctionResponse> auctionResponses = item.getAuctions().stream()
                .map(auction -> ItemAuctionResponse.builder()
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
                        .build())
                .collect(Collectors.toList());

        // Ánh xạ loại đấu giá
        AuctionTypeResponse auctionTypeResponse = item.getAuctionType() != null ?
                AuctionTypeResponse.builder()
                        .auctionTypeId(item.getAuctionType().getAuctionTypeId())
                        .auctionTypeName(item.getAuctionType().getAuctionTypeName())
                        .build() : null;

        // Ánh xạ danh mục phụ
        SubCategoryItemResponse subCategoryResponse = item.getSubCategory() != null ?
                SubCategoryItemResponse.builder()
                        .subCategoryId(item.getSubCategory().getSubCategoryId())
                        .subCategory(item.getSubCategory().getSubCategory())
                        .build() : null;

        // Trả về ItemDetailResponse
        return ItemDetailResponse.builder()
                .itemId(item.getItemId())
                .thumbnail(item.getThumbnail())
                .itemName(item.getItemName())
                .itemDescription(item.getItemDescription())
                .itemCondition(item.getItemCondition())
                .itemStatus(item.getItemStatus())
                .priceBuyNow(item.getPriceBuyNow())
                .reason(item.getReason())
                .auction(auctionResponses) // Ánh xạ danh sách phiên đấu giá
                .images(imageResponses)
                .itemDocument(item.getItemDocument())
                .priceStepItem(item.getPriceStepItem())
                .auctionType(auctionTypeResponse)
                .numberParticipant(0) // Giả sử số lượng người tham gia là 0
                .scId(subCategoryResponse)
                .build();
    }

    // Phương thức ánh xạ Item sang ItemResponse cho danh sách sản phẩm
    public ItemResponse toItemResponse(Item item) {
        // Ánh xạ Auction nếu tồn tại
        List<ItemAuctionResponse> auctionResponses = item.getAuctions().stream()
                .map(auction -> ItemAuctionResponse.builder()
                        .auctionId(auction.getAuctionId())
                        .startTime(auction.getStartTime())
                        .endTime(auction.getEndTime())
                        .startPrice(auction.getStartPrice())
                        .approveAt(auction.getApproveAt())
                        .createBy(auction.getCreateBy())
                        .startDate(auction.getStartDate())
                        .endDate(auction.getEndDate())
                        .status(auction.getStatus())
                        .build())
                .collect(Collectors.toList());

        // Ánh xạ loại đấu giá nếu tồn tại
        AuctionTypeResponse auctionTypeResponse = item.getAuctionType() != null ?
                AuctionTypeResponse.builder()
                        .auctionTypeId(item.getAuctionType().getAuctionTypeId())
                        .auctionTypeName(item.getAuctionType().getAuctionTypeName())
                        .build() : null;

        // Ánh xạ danh mục phụ nếu tồn tại
        SubCategoryItemResponse subCategoryResponse = item.getSubCategory() != null ?
                SubCategoryItemResponse.builder()
                        .subCategoryId(item.getSubCategory().getSubCategoryId())
                        .subCategory(item.getSubCategory().getSubCategory())
                        .build() : null;

        // Ánh xạ danh sách ảnh
        List<ImageItemResponse> imageResponses = Optional.ofNullable(item.getImageItems())
                .orElse(Collections.emptyList())
                .stream()
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
                .auction(auctionResponses) // Ánh xạ danh sách các phiên đấu giá
                .auctionTypeResponse(auctionTypeResponse)
                .scId(subCategoryResponse)
                .imageItemResponse(imageResponses) // Truyền danh sách ảnh
                .itemDocument(item.getItemDocument())
                .priceStepItem(item.getPriceStepItem())
                .createBy(item.getCreateBy())
                .createAt(String.valueOf(item.getCreateAt()))
                .updateAt(String.valueOf(item.getUpdateAt()))
                .build();
    }

}
