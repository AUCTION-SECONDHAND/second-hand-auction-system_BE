package com.second_hand_auction_system.dtos.responses.item;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.second_hand_auction_system.dtos.responses.BaseResponse;
import com.second_hand_auction_system.dtos.responses.auction.ItemAuctionResponse;
import com.second_hand_auction_system.dtos.responses.auctionType.AuctionTypeResponse;
import com.second_hand_auction_system.dtos.responses.subCategory.SubCategoryItemResponse;
import com.second_hand_auction_system.models.Auction;
import com.second_hand_auction_system.models.FeedBack;
import com.second_hand_auction_system.models.ItemSpecific;
import com.second_hand_auction_system.utils.ItemStatus;
import jakarta.validation.constraints.NotBlank;
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

    private String itemName;

    private String itemDescription;

    private String itemCondition;

    private ItemStatus itemStatus;

    private String brandName;

    private String thumbnail;

    private String imgItem;

    private String createBy;

    private String updateBy;

    @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
    private String createAt;

    @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
    private String updateAt;

    private ItemSpecificResponse itemSpecific;

    private List<ImageItemResponse> imageItemResponse;

    private SubCategoryItemResponse scId;

    private ItemAuctionResponse auction;

    private AuctionTypeResponse auctionTypeResponse;

//    private Integer userId;

//    private FeedBack feedback;

//    private Auction auction;


//    private Integer scId;
}
