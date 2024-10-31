package com.second_hand_auction_system.dtos.responses.item;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ItemBriefResponseOrder {
    private Integer itemId;
    private String itemName;
    private String thumbnail;
    private String sellerName;
}
