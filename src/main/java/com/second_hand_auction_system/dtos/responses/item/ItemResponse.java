package com.second_hand_auction_system.dtos.responses.item;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.second_hand_auction_system.dtos.responses.BaseResponse;
import com.second_hand_auction_system.dtos.responses.auction.ItemAuctionResponse;
import com.second_hand_auction_system.dtos.responses.auctionType.AuctionTypeResponse;
import com.second_hand_auction_system.dtos.responses.subCategory.SubCategoryItemResponse;
import com.second_hand_auction_system.utils.ItemStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemResponse extends BaseResponse {
    private Integer itemId;

    @NotNull
    private String itemName;

    @Size(max = 1000)
    private String itemDescription;

    private String itemCondition;

    private ItemStatus itemStatus;

    @NotNull
    private Double priceBuyNow;

    private String thumbnail;

    private Double priceStepItem;

    private String itemDocument;

    private String createBy;

    private String updateBy;

    @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
    private String createAt;

    @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
    private String updateAt;

    private List<ImageItemResponse> imageItemResponse;

    private SubCategoryItemResponse scId;

    private List<ItemAuctionResponse> auction;  // Changed to List for multiple auctions

    private AuctionTypeResponse auctionTypeResponse;
}
