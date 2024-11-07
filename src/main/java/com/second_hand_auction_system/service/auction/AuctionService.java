package com.second_hand_auction_system.service.auction;

import com.second_hand_auction_system.dtos.request.auction.AuctionDto;
import com.second_hand_auction_system.dtos.responses.ResponseObject;
import com.second_hand_auction_system.dtos.responses.auction.AuctionResponse;
import com.second_hand_auction_system.models.*;
import com.second_hand_auction_system.repositories.*;
import com.second_hand_auction_system.service.jwt.IJwtService;
import com.second_hand_auction_system.utils.*;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

import static com.second_hand_auction_system.utils.AuctionStatus.CANCELLED;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionService implements IAuctionService {
    private final AuctionRepository auctionRepository;
    private final ItemRepository itemRepository;
    private final ModelMapper modelMapper;
    private static final Logger logger = LoggerFactory.getLogger(AuctionService.class);
    private final BidRepository bidRepository;
    private final AuctionTypeRepository auctionTypeRepository;
    private final IJwtService jwtService;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public void addAuction(@Valid AuctionDto auctionDto) throws Exception {
        Item itemExist = itemRepository.findById(auctionDto.getItem())
                .orElseThrow(() -> new Exception("Item not found"));
        AuctionType auctionType = auctionTypeRepository.findById(auctionDto.getAuctionTypeId())
                .orElseThrow(() -> new Exception("Auction type not found"));
        if (!auctionType.getAuctionTypeName().equals(itemExist.getAuctionType().getAuctionTypeName())) {
            throw new Exception("Auction type does not match the item's auction type");
        }

        if (auctionDto.getStartTime().after(auctionDto.getEndTime())) {
            throw new Exception("Start time must be before end time");
        }
        if (auctionDto.getStartPrice() < 0) {
            throw new Exception("Start price must be a non-negative value");
        }
        if (auctionDto.getBuyNowPrice() < 0) {
            throw new Exception("Buy now price must be a non-negative value");
        }
        String authHeader = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest().getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new Exception("Unauthorized");
        }
        String token = authHeader.substring(7);
        String userEmail = jwtService.extractUserEmail(token);
        User requester = userRepository.findByEmailAndStatusIsTrue(userEmail).orElse(null);
        if (requester == null) {
            throw new Exception("User not found");
        }
        Auction auction = modelMapper.map(auctionDto, Auction.class);
        auction.setCreateBy(itemExist.getCreateBy());
        auction.setApproveBy(requester.getFullName());
        auction.setItem(itemExist);
        auction.setAuctionType(auctionType);
        Wallet wallet = Wallet.builder()
                .balance(0)
                .walletType(WalletType.AUCTION)
                .statusWallet(StatusWallet.ACTIVE)
                .user(null)
                .build();
        walletRepository.save(wallet);
        auction.setWallet(wallet);
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


    @Scheduled(fixedDelay = 5000) // 1 giờ
    @Transactional
    public void closeExpiredAuctions() {
        List<Auction> auctions = auctionRepository.findAll();

        for (Auction auction : auctions) {
            if (!auction.getStatus().equals(AuctionStatus.CLOSED) &&
                    LocalDateTime.now().isAfter(auction.getEndDate().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime()
                            .with(auction.getEndTime().toLocalTime()))) {

                // Cập nhật trạng thái đấu giá thành CLOSED
                auction.setStatus(AuctionStatus.CLOSED);
                auctionRepository.save(auction);
                log.info("THONG BAO DA KET THUC CAC PHIEN DAU GIA: " + auction.getAuctionId());
                // Tìm tất cả các bid liên quan đến phiên đấu giá này
                List<Bid> bids = bidRepository.findByAuction_AuctionId(auction.getAuctionId());

                // Hoàn tiền cho tất cả người đặt giá
                for (Bid bid : bids) {
                    // Lấy ví của người dùng tham gia đấu giá thông qua Bid -> User -> Wallet
                    Wallet userWallet = bid.getUser().getWallet();

                    // Kiểm tra ví của người dùng có tồn tại không
                    if (userWallet != null) {
                        // Cập nhật số dư ví của người dùng
                        userWallet.setBalance(userWallet.getBalance() + bid.getBidAmount());
                        walletRepository.save(userWallet); // Lưu thay đổi ví của người dùng

                        Transaction refundTransaction = Transaction.builder()
                                .wallet(userWallet)
                                .transactionStatus(TransactionStatus.COMPLETED)
                                .description("Hoan coc nguoi dung")
                                .commissionRate(0)
                                .commissionAmount(0)
                                .transactionType(TransactionType.REFUND)
                                .recipient(userWallet.getUser().getFullName())
                                .sender("SYSTEM")
                                .transactionWalletCode(random())
                                .build();
                        transactionRepository.save(refundTransaction);
                    } else {
                        System.out.println("User does not have a wallet: " + bid.getUser().getEmail());
                    }
                }
            }
        }
    }

    private long random (){
        Random random = new Random();
        int number = random.nextInt(900000) + 100000;
        return Long.parseLong(String.valueOf(number));

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
//                .comment(auction.getComment())
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
