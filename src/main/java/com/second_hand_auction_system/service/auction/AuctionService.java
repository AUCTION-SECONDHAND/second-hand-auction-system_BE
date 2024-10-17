package com.second_hand_auction_system.service.auction;

import com.second_hand_auction_system.dtos.request.auction.AuctionDto;
import com.second_hand_auction_system.dtos.responses.ResponseObject;
import com.second_hand_auction_system.dtos.responses.auction.ListAuction;
import com.second_hand_auction_system.models.Auction;
import com.second_hand_auction_system.models.Item;
import com.second_hand_auction_system.repositories.AuctionRepository;
import com.second_hand_auction_system.repositories.ItemRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static com.second_hand_auction_system.utils.AuctionStatus.CANCELLED;

@Service
@RequiredArgsConstructor
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

//    @Override
//    public ResponseEntity<?> getAll() {
//        List<Auction> auctions = auctionRepository.findAll();
//
//        if (auctions.isEmpty()) {
//            for (Auction auction : auctions) {
//                ListAuction listAuction = modelMapper.map(auction, ListAuction.class);
//
//            }
//        }
//        return ResponseEntity.ok(ResponseObject.builder()
//                        .status(HttpStatus.OK)
//                        .message("List of auctions")
//                        .data(ListAuction.builder().auctionDtoList(ListAuction.builder().build().getAuctionDtoList()))
//                .build());
//    }


}
