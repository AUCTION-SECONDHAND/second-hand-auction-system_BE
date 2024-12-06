package com.second_hand_auction_system.dtos.responses.auction;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ResponseAuction {
    private String itemName;
    private String thumbnail;
    private String title;
    private String description;
    private String seller;
    private Double amount;
}
