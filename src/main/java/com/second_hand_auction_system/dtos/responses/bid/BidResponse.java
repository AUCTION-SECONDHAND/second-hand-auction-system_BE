package com.second_hand_auction_system.dtos.responses.bid;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BidResponse {

    private int bidAmount;
    private LocalDateTime bidTime;
//    private String bidStatus;
    private boolean winBid;
    private Integer userId;
    private Integer auctionId;
    private String username;
    private String email;
}
