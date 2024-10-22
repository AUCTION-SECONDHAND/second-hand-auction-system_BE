package com.second_hand_auction_system.service.auctiontype;

import com.second_hand_auction_system.dtos.request.auctiontype.AuctionTypeDTO;
import com.second_hand_auction_system.dtos.responses.ResponseObject;
import com.second_hand_auction_system.models.AuctionType;
import com.second_hand_auction_system.repositories.AuctionTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuctionTypeSerivce implements IAuctionTypeService {
    private final AuctionTypeRepository auctionTypeRepository;

    @Override
    public ResponseEntity<?> createAuctionType(AuctionTypeDTO auctionType) {
        // Check if auctionTypeName is empty or null
//        if (auctionType.getAuctionTypeName() == null || auctionType.getAuctionTypeName().isBlank()) {
////            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseObject.builder()
////                    .status(HttpStatus.BAD_REQUEST)
////                    .message("AuctionType name cannot be empty")
////                    .data(null)
////                    .build());
////        }


        if (auctionTypeRepository.existsByAuctionTypeName(auctionType.getAuctionTypeName())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseObject.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("AuctionType already exists")
                    .data(null)
                    .build());
        }

        // Create and save a new AuctionType
        AuctionType type = AuctionType.builder()
                .auctionTypeName(auctionType.getAuctionTypeName())
                .auctionTypeDescription(auctionType.getAuctionTypeDescription())
                .build();
        auctionTypeRepository.save(type);

        // Return success response
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseObject.builder()
                .status(HttpStatus.CREATED)
                .message("AuctionType successfully created")
                .data(type)
                .build());
    }


    @Override
    public ResponseEntity<?> update(AuctionTypeDTO auctionType, int id) {
        var type = auctionTypeRepository.findById(id).orElseThrow(null);
        if (type == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder().status(HttpStatus.NOT_FOUND)
                    .message("AuctionType not found")
                    .data(null)
                    .build());
        }
        type.setAuctionTypeDescription(auctionType.getAuctionTypeDescription());
        type.setAuctionTypeName(auctionType.getAuctionTypeName());
        if (auctionTypeRepository.existsByAuctionTypeName(auctionType.getAuctionTypeName())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseObject.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("AuctionType already exists")
                    .data(null)
                    .build());
        }
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
        return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder().status(HttpStatus.OK)
                .message("Delete auction type successfully")
                .data(null)
                .build());
    }

    @Override
    public ResponseEntity<?> getById(int id) {
        var auctionType = auctionTypeRepository.findById(id).orElseThrow(null);
        if (auctionType == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder().status(HttpStatus.NOT_FOUND)
                    .message("AuctionType not found")
                    .data(null)
                    .build());
        }
        return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder().status(HttpStatus.OK)
                .message("AuctionType found")
                .data(auctionType)
                .build());
    }

    @Override
    public ResponseEntity<?> getAuctions(int size, int page) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuctionType> auctionTypes = auctionTypeRepository.findAll(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder().status(HttpStatus.OK)
                .message("Auctions found")
                .data(auctionTypes)
                .build());
    }
}
