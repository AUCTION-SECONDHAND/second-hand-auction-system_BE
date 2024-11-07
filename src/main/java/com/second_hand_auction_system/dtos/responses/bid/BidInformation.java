package com.second_hand_auction_system.dtos.responses.bid;

import com.second_hand_auction_system.models.Auction;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BidInformation {
    private int amountBid;
    private Auction auction;
}
