package com.second_hand_auction_system.dtos.responses.auction;

import com.second_hand_auction_system.models.AuctionType;
import com.second_hand_auction_system.utils.AuctionStatus;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AuctionOrder {
    private Integer auctionId;
    private double priceStep;
    private String termConditions;
    private AuctionStatus status;
    private String auctionTypeName;
}
