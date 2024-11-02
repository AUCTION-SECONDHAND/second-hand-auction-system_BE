package com.second_hand_auction_system.service.auction;

import com.second_hand_auction_system.dtos.request.auction.AuctionDto;
import com.second_hand_auction_system.dtos.responses.ResponseObject;
import com.second_hand_auction_system.dtos.responses.auction.AuctionResponse;
import com.second_hand_auction_system.models.Auction;
import com.second_hand_auction_system.models.Bid;
import com.second_hand_auction_system.models.Item;
import com.second_hand_auction_system.repositories.AuctionRepository;
import com.second_hand_auction_system.repositories.BidRepository;
import com.second_hand_auction_system.repositories.ItemRepository;
import com.second_hand_auction_system.utils.AuctionStatus;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.second_hand_auction_system.utils.AuctionStatus.CANCELLED;

@Service
@RequiredArgsConstructor
public class AuctionService implements IAuctionService {
    private final AuctionRepository auctionRepository;
    private final ItemRepository itemRepository;
    private final ModelMapper modelMapper;
    private static final Logger logger = LoggerFactory.getLogger(AuctionService.class);
    private final BidRepository bidRepository;

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

            List<AuctionResponse> auctionResponses = auctions.stream()
                    .map(this::convertToAuctionResponse) // Dùng phương thức ánh xạ thủ công
                    .collect(Collectors.toList());


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


    @Scheduled(fixedRate = 50000)
    public void checkAuctionStatus() {
        Date currentDate = new Date(System.currentTimeMillis());
        Time currentTime = new Time(System.currentTimeMillis());

        // Tìm tất cả các phiên đấu giá có ngày và giờ kết thúc đã qua và trạng thái là OPEN
        List<Auction> auctions = auctionRepository.findAllByEndDateBeforeOrEndDateEqualsAndEndTimeBeforeAndStatus(
                currentDate, currentDate, currentTime, AuctionStatus.OPEN
        );

        for (Auction auction : auctions) {
            auction.setStatus(AuctionStatus.CLOSED); // Cập nhật trạng thái thành CLOSED
            auctionRepository.save(auction);
            logger.info("Auction with ID {} has been closed.", auction.getAuctionId());
        }
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
