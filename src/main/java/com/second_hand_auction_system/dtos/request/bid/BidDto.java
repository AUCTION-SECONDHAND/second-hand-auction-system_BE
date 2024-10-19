package com.second_hand_auction_system.dtos.request.bid;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BidDto {
    @NotNull(message = "Bid ID is required")
    private Integer bidId;

    @NotNull(message = "Bid amount is required")
    private int bidAmount;

    @NotNull(message = "Bid time is required")
    private int bidTime;

    @NotBlank(message = "Bid status is required")
    private String bidStatus;

    @NotNull(message = "Win bid status is required")
    private boolean winBid;

    @NotNull(message = "User ID is required")
    @JsonProperty
    private Integer userId;

    @NotNull(message = "Auction ID is required")
    @JsonProperty
    private Integer auctionId;

}