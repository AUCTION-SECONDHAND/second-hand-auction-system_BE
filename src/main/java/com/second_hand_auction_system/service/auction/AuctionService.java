package com.second_hand_auction_system.service.auction;

import com.second_hand_auction_system.dtos.request.auction.AuctionDto;
import com.second_hand_auction_system.dtos.responses.ResponseObject;
import com.second_hand_auction_system.dtos.responses.auction.AuctionResponse;
import com.second_hand_auction_system.dtos.responses.auction.ListAuction;
import com.second_hand_auction_system.models.Auction;
import com.second_hand_auction_system.models.Item;
import com.second_hand_auction_system.repositories.AuctionRepository;
import com.second_hand_auction_system.repositories.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.second_hand_auction_system.utils.AuctionStatus.CANCELLED;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionService implements IAuctionService {
    private final AuctionRepository auctionRepository;
    private final ItemRepository itemRepository;
    private final ModelMapper modelMapper;

    @Override
    public void addAuction(AuctionDto auctionDto) throws Exception {
        Item itemExist = itemRepository.findById(auctionDto.getItem())
                .orElseThrow(() -> new Exception("Item not found"));
        Auction auction = modelMapper.map(auctionDto, Auction.class);
        auction.setItem(itemExist);
        auctionRepository.save(auction);
    }

    @Override
    public void updateAuction(int auctionId, AuctionDto auctionDto) throws Exception {
        Item itemExist = itemRepository.findById(auctionDto.getItem())
                .orElseThrow(() -> new Exception("Item not found"));
        Auction auctionExist = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new Exception("Auction not found"));
        modelMapper.map(auctionDto, auctionExist);
        auctionExist.setItem(itemExist);
        auctionRepository.save(auctionExist);
    }

    @Override
    public void removeAuction(int auctionId) throws Exception {
        Auction auctionExist = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new Exception("Auction not found"));
        auctionExist.setStatus(CANCELLED);
        auctionRepository.save(auctionExist);
    }

//    @Override
//    public ResponseEntity<List<AuctionDto>> getAllAuctions(int page, int size) {
//        Pageable pageable = PageRequest.of(page, size);
//        Page<Auction> auctions = auctionRepository.findAll(pageable);
//        List<AuctionDto> auctionDtoList = auctions.getContent().stream()
//                .map(auction -> modelMapper.map(auction, AuctionDto.class))
//                .collect(Collectors.toList());
//        ResponseObject responseObject = ResponseObject.<List<AuctionDto>>builder()
//                .data(auctionDtoList)
//                .message("List of auctions")
//                .status(HttpStatus.OK)
//                .build();
//
//        return ResponseEntity.ok((List<AuctionDto>) responseObject);
//    }

    @Override
    public ResponseEntity<?> getAll() {
        try {
            List<Auction> auctions = auctionRepository.findAll();
            auctions.forEach(auction -> log.info("Auction data: {}", auction));

            List<AuctionResponse> auctionResponses = auctions.stream()
                    .map(this::convertToAuctionResponse) // Dùng phương thức ánh xạ thủ công
                    .collect(Collectors.toList());

            auctionResponses.forEach(response -> log.info("Mapped AuctionResponse: {}", response));

            return ResponseEntity.ok(ResponseObject.builder()
                    .status(HttpStatus.OK)
                    .message("List of auctions")
                    .data(auctionResponses)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(ResponseObject.builder()
                .status(HttpStatus.OK)
                .message("List of auctions")
                .data(null)
                .build());
    }

    private AuctionResponse convertToAuctionResponse(Auction auction) {
        return AuctionResponse.builder()
                .startTime(auction.getStartTime())
                .endTime(auction.getEndTime())
                .startPrice(auction.getStartPrice())
                .description(auction.getDescription())
                .termConditions(auction.getTermConditions())
                .priceStep(auction.getPriceStep())
                .shipType(auction.getShipType())
                .comment(auction.getComment())
                .status(auction.getStatus())
                .approveBy(auction.getApproveBy())
                .approveAt(auction.getApproveAt())
                .createBy(auction.getCreateBy())
                .item(auction.getItem().getItemId())
                .createBy(auction.getCreateBy())
//                .createdAt(auction.getCreateAt())
                .build();
    }


}
