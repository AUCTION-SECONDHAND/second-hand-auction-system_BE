package com.second_hand_auction_system.dtos.responses.auction;

import com.second_hand_auction_system.dtos.request.auction.AuctionDto;
import com.second_hand_auction_system.models.Auction;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ListAuction {
    private List<AuctionDto> auctionDtoList;
}
