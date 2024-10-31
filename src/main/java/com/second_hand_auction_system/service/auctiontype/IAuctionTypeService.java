package com.second_hand_auction_system.service.auctiontype;

import com.second_hand_auction_system.dtos.request.auctiontype.AuctionTypeDTO;
import com.second_hand_auction_system.dtos.responses.auctionType.AuctionTypeResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface IAuctionTypeService {
    ResponseEntity<?> createAuctionType(@Valid AuctionTypeDTO auctionType);

    ResponseEntity<?> update(@Valid AuctionTypeDTO auctionType,int id);

    ResponseEntity<?> delete(int id);

    ResponseEntity<?> getById(int id);

    ResponseEntity<?> getAuctions(int size, int page);

    List<AuctionTypeResponse> getAuctionTypes() throws Exception;
}
