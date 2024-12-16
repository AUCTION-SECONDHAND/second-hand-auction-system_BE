package com.second_hand_auction_system.dtos.responses.item;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.second_hand_auction_system.dtos.responses.auction.ItemAuctionResponse;
import com.second_hand_auction_system.dtos.responses.auctionType.AuctionTypeResponse;
import com.second_hand_auction_system.dtos.responses.subCategory.SubCategoryItemResponse;
import com.second_hand_auction_system.models.ImageItem;
import com.second_hand_auction_system.utils.AuctionStatus;
import com.second_hand_auction_system.utils.ItemCondition;
import com.second_hand_auction_system.utils.ItemStatus;
import jakarta.persistence.Column;
import lombok.*;

import java.sql.Time;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ItemDetailResponse {

    private Integer itemId;

    private String itemName;

    private String itemDescription;

    private String thumbnail;

    private ItemStatus itemStatus;

    private Double priceBuyNow;

    private String reason;
    private ItemCondition itemCondition;

    private ItemAuctionResponse auction;

    private SubCategoryItemResponse scId;

    private AuctionTypeResponse auctionType;

    private ItemSpecificResponse itemSpecific;

    private List<ImageItemResponse> images;

    private int numberParticipant;

    private Integer checkBid;
}
