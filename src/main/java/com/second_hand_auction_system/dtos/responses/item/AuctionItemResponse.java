package com.second_hand_auction_system.dtos.responses.item;

import com.second_hand_auction_system.dtos.responses.BaseResponse;
import com.second_hand_auction_system.dtos.responses.auction.ItemAuctionResponse;
import com.second_hand_auction_system.dtos.responses.auctionType.AuctionTypeResponse;
import com.second_hand_auction_system.dtos.responses.subCategory.SubCategoryItemResponse;
import com.second_hand_auction_system.utils.ItemStatus;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuctionItemResponse extends BaseResponse {
    private Integer itemId;

    private String itemName;

    private String itemDescription;

    private String thumbnail;

    private ItemStatus itemStatus;
    private Double priceBuyNow;

    private ItemAuctionResponse auction;

    private SubCategoryItemResponse scId;

    private AuctionTypeResponse auctionTypeId;
}
