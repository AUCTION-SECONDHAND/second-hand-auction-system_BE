package com.second_hand_auction_system.service.auctiontype;

import com.second_hand_auction_system.dtos.request.auctiontype.AuctionTypeDTO;
import com.second_hand_auction_system.dtos.responses.ResponseObject;
import com.second_hand_auction_system.models.AuctionType;
import com.second_hand_auction_system.repositories.AuctionTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuctionTypeSerivce implements IAuctionTypeService {
    private final AuctionTypeRepository auctionTypeRepository;

    @Override
    public ResponseEntity<?> createAuctionType(AuctionTypeDTO auctionType) {
        AuctionType type = AuctionType.builder()
                .auctionTypeDescription(auctionType.getAuctionTypeDescription())
                .auctionTypeName(auctionType.getAuctionTypeName())
                .build();
        auctionTypeRepository.save(type);
        if (auctionTypeRepository.existsByAuctionTypeName(auctionType.getAuctionTypeName())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseObject.builder().status(HttpStatus.BAD_REQUEST)
                    .message("AuctionType existed")
                    .data(null)
                    .build());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseObject.builder().status(HttpStatus.BAD_REQUEST)
                .message("AuctionType successfully created")
                .data(type)
                .build());
    }

    @Override
    public ResponseEntity<?> update(AuctionTypeDTO auctionType,int id) {
        var type = auctionTypeRepository.findById(id).orElseThrow(null);
        if (type == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder().status(HttpStatus.NOT_FOUND)
                    .message("AuctionType not found")
                    .data(null)
                    .build());
        }
        type.setAuctionTypeDescription(auctionType.getAuctionTypeDescription());
        type.setAuctionTypeName(auctionType.getAuctionTypeName());
        auctionTypeRepository.save(type);
        return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder().status(HttpStatus.OK)
                .message("Updated AuctionType")
                .data(type)
                .build());
    }

    @Override
    public ResponseEntity<?> delete(int id) {
        var type = auctionTypeRepository.findById(id).orElseThrow(null);
        if (type == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder().status(HttpStatus.NOT_FOUND)
                    .message("AuctionType not found")
                    .data(null)
                    .build());
        }
        auctionTypeRepository.delete(type);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder().status(HttpStatus.NOT_FOUND)
                .message("Delete auction type successfully")
                .data(null)
                .build());
    }
}
