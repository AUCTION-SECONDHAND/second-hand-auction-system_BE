package com.second_hand_auction_system.dtos.request.bid;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BidRequest {

    @NotNull(message = "Bid amount cannot be null")
    @Min(value = 1, message = "Bid amount must be greater than or equal to 1")
    private Integer bidAmount;

//    private LocalDateTime bidTime;

    @NotNull(message = "Auction ID cannot be null")
    private Integer auctionId;
}
