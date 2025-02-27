package com.second_hand_auction_system.dtos.responses.item;

import com.second_hand_auction_system.utils.ItemStatus;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class ItemResponse2 {
    private Integer itemId;

    private String itemName;

    private String itemDescription;

    private String itemCondition;

    private ItemStatus itemStatus;

    private Double priceBuyNow;

    private String thumbnail;

    private String imgItem;

    private String createBy;


    private Integer scId;
    private LocalDateTime create_at;
    private LocalDateTime  update_at;
}
