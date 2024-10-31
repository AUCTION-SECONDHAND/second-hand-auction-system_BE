package com.second_hand_auction_system.dtos.responses.auctionType;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AuctionTypeResponse {
    @JsonProperty("act_id")
    private Integer auctionTypeId;

    @JsonProperty("auction_typeName")
    private String auctionTypeName;
}
