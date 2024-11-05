package com.second_hand_auction_system.dtos.responses.auctionRegistrations;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.second_hand_auction_system.utils.Registration;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CheckStatusAuctionRegisterResponse {
    private Integer userId;
    @JsonProperty("registration_status")
    private Registration registration;
    private Integer auctionId;
    private Boolean statusRegistration;

    public CheckStatusAuctionRegisterResponse(Integer userId, Integer auctionId,Registration registration ) {
        this.userId = userId;
        this.auctionId = auctionId;
        this.registration = registration;
//        this.statusRegistration = statusRegistration;
    }
}
