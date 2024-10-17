package com.second_hand_auction_system.dtos.request.auctionRegistrations;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.second_hand_auction_system.models.User;
import com.second_hand_auction_system.utils.Registration;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuctionRegistrationsDto {
    @JsonProperty("deposit_amount")
    @DecimalMin(value = "0.0", inclusive = false, message = "Deposit amount must be greater than 0")
    private double depositAmount;

    @JsonProperty("registration")
    @NotNull(message = "Registration status is required")
    @Enumerated(EnumType.STRING)
    private Registration registration;

    @JsonProperty("note")
    @NotBlank(message = "Note cannot be blank")
    private String note;

    @JsonProperty("auction_id")
    @NotNull(message = "Auction ID is required")
    private Integer auction;


}
