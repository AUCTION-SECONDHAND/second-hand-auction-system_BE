package com.second_hand_auction_system.dtos.request.auctiontype;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Getter
@Setter
public class AuctionTypeDTO {
    @NotBlank(message = "Auction type name cannot be blank.")
    @Size(max = 100, message = "Auction type name must not exceed 100 characters.")
    private String auctionTypeName;

    @NotBlank(message = "Auction type description cannot be blank.")
    @Size(max = 255, message = "Auction type description must not exceed 255 characters.")
    private String auctionTypeDescription;
}
