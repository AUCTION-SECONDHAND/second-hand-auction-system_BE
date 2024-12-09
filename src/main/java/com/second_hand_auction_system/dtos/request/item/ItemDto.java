package com.second_hand_auction_system.dtos.request.item;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.second_hand_auction_system.models.*;
import com.second_hand_auction_system.utils.ItemCondition;
import com.second_hand_auction_system.utils.ItemStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemDto {
    //private Integer itemId;

    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 200, message = "Product name must be between 3 and 200 characters")
    @JsonProperty("item_name")
    private String itemName;

    @NotBlank(message = "Item description is required")
//    @Size(min = 10, max = 500, message = "Item description must be between 10 and 500 characters")
    @JsonProperty("item_description")
    private String itemDescription;

    @NotNull(message = "Item condition is required")
    @JsonProperty("item_condition")
    private ItemCondition itemCondition;


    @JsonProperty("itemDocument")
    private String itemDocument;

    @NotNull(message = "Giá mua ngay không được để trống.")
    @JsonProperty("price_buy_now")
    private Double priceBuyNow;


    @JsonProperty("img_item")
    private List<ImgItemDto> imgItem;

    @NotNull(message = "SubCategory ID is required")
    @JsonProperty("sc_id")
    private Integer scId;

    @NotNull(message = "AuctionType ID is required")
    @JsonProperty("auction_type")
    private Integer auctionType;
}
