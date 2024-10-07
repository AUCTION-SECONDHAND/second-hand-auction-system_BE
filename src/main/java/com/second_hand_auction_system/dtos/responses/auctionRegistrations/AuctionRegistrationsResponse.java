package com.second_hand_auction_system.dtos.responses.auctionRegistrations;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.second_hand_auction_system.dtos.responses.BaseResponse;
import com.second_hand_auction_system.utils.Registration;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuctionRegistrationsResponse extends BaseResponse {
    @JsonProperty("ar_id")
    private Integer auctionRegistrationId;

    @JsonProperty("deposite_amount")
    private double depositeAmount;

    @JsonProperty("registration")
    private Registration registration;

    @JsonProperty("note")
    private String note;

    @JsonProperty("user_id")
    private Integer user;
}
