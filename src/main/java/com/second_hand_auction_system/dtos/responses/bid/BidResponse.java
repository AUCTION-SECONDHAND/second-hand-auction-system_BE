package com.second_hand_auction_system.dtos.responses.bid;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BidResponse {

    private Integer bidId;
    private int bidAmount;
    private int bidTime;
    private String bidStatus;
    private boolean winBid;
    private Integer userId;
    private Integer auctionId;
    private String username;
}
