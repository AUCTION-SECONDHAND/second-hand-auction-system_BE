package com.second_hand_auction_system.service.auction;

import com.second_hand_auction_system.dtos.request.auction.AuctionDto;
import com.second_hand_auction_system.dtos.responses.ResponseObject;
import com.second_hand_auction_system.dtos.responses.auction.AuctionResponse;
import com.second_hand_auction_system.dtos.responses.auction.ResponseAuction;
import com.second_hand_auction_system.dtos.responses.item.ItemSpecificResponse;
import com.second_hand_auction_system.models.*;
import com.second_hand_auction_system.repositories.*;
import com.second_hand_auction_system.service.email.EmailService;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
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
    private final EmailService emailService;
    private final AuctionRegistrationsRepository auctionRegistrationsRepository;

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
        auction.setApproveAt(new Date());
        auction.setItem(itemExist);
        auction.setStatus(AuctionStatus.OPEN);
        auction.setAuctionType(auctionType);
        Wallet wallet = Wallet.builder()
                .balance(0)
                .walletType(WalletType.AUCTION)
                .statusWallet(StatusWallet.ACTIVE)
                .user(null)
                .build();
        walletRepository.save(wallet);
        auction.setWallet(wallet);
        itemExist.setItemStatus(ItemStatus.ACCEPTED);
        itemRepository.save(itemExist);
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

    @Override
    public ResponseEntity<?> getAuctionById(Integer auctionId) {
        var auction = auctionRepository.findById(auctionId).orElse(null);
        Double maxBid = auctionRepository.findMaxBidByAuctionId(auctionId);
        if(auction!=null){
            ResponseAuction responseAuction = ResponseAuction.builder()
                    .itemName(auction.getItem().getItemName())
                    .title(auction.getItem().getBrandName())
                    .amount(Double.valueOf(String.valueOf(maxBid)))
                    .seller(auction.getCreateBy())
                    .thumbnail(auction.getItem().getThumbnail())
                    .description(auction.getDescription())
                    .itemSpecific( ItemSpecificResponse.builder()
                            .color(auction.getItem().getItemSpecific().getColor())
                            .type(auction.getItem().getItemSpecific().getType())
                            .dimension(auction.getItem().getItemSpecific().getDimension())
                            .itemSpecId(auction.getItem().getItemSpecific().getItemSpecificId())
                            .manufactureDate(auction.getItem().getItemSpecific().getManufactureDate())
                            .material(auction.getItem().getItemSpecific().getMaterial())
                            .original(auction.getItem().getItemSpecific().getOriginal())
                            .weight(auction.getItem().getItemSpecific().getWeight())
                            .percent(auction.getItem().getItemSpecific().getPercent())
                            .build())
                    .build();
            return ResponseEntity.ok(ResponseObject.builder()
            .status(HttpStatus.OK)
                    .message("Auction found")
                    .data(responseAuction)
                    .build());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                .status(HttpStatus.NOT_FOUND)
                .message("Auction not found")
                .data(null)
                .build());
    }


    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void closeExpiredAuctions() {
        List<Auction> auctions = auctionRepository.findAll();

        for (Auction auction : auctions) {
            if (auction.getStatus() != null && !auction.getStatus().equals(AuctionStatus.CLOSED) &&
                    LocalDateTime.now().isAfter(auction.getEndDate().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime()
                            .with(auction.getEndTime().toLocalTime()))) {

                // Đóng phiên đấu giá
                auction.setStatus(AuctionStatus.CLOSED);
                auctionRepository.save(auction);
                log.info("THONG BAO DA KET THUC CAC PHIEN DAU GIA: " + auction.getAuctionId());

                // Lấy danh sách bid và người thắng
                List<Bid> bids = bidRepository.findByAuction_AuctionId(auction.getAuctionId());
                Bid winningBid = getWinningBid(bids);

                // Lấy ví cọc của phiên đấu giá
                Wallet walletAuction = auction.getWallet();
                if (walletAuction == null) {
                    log.error("Không tìm thấy ví cọc cho phiên đấu giá: " + auction.getAuctionId());
                    continue;  // Bỏ qua phiên đấu giá này nếu không có ví cọc
                }

                List<String> refundedUsers = new ArrayList<>();
                List<String> losersEmails = new ArrayList<>();

                // Hoàn tiền cho các người thua và giữ lại cọc cho người thắng
                for (Bid bid : bids) {
                    Wallet userWallet = bid.getUser().getWallet();
                    double depositAmount = 0.1 * auction.getPriceStep(); // Số tiền cọc cho mỗi người dùng

                    if (userWallet != null) {
                        try {
                            if (!bid.equals(winningBid)) {
                                // Trừ cọc khỏi ví đấu giá và hoàn tiền vào ví người thua
                                walletAuction.setBalance(walletAuction.getBalance() - depositAmount);
                                userWallet.setBalance(userWallet.getBalance() + depositAmount);
                                walletRepository.save(walletAuction);
                                walletRepository.save(userWallet);

                                // Tạo giao dịch hoàn tiền
                                Transaction refundTransaction = Transaction.builder()
                                        .wallet(userWallet)
                                        .transactionStatus(TransactionStatus.COMPLETED)
                                        .description("Hoàn cọc cho người thua cuộc")
                                        .transactionType(TransactionType.REFUND)
                                        .recipient(userWallet.getUser().getFullName())
                                        .sender("SYSTEM")
                                        .amount((long) +depositAmount)
                                        .transactionWalletCode(random())
                                        .build();
                                transactionRepository.save(refundTransaction);

                                refundedUsers.add(bid.getUser().getEmail() + " (số tiền: " + depositAmount + ")");
                                losersEmails.add(bid.getUser().getEmail());
                            } else {
                                // Giữ lại cọc của người thắng

                                log.info("Giữ lại cọc cho người thắng: " + bid.getUser().getEmail());
                                emailService.sendWinnerNotification(bid.getUser().getEmail(), winningBid);
                            }
                        } catch (Exception e) {
                            log.error("Lỗi khi xử lý cọc cho user " + bid.getUser().getEmail() +
                                    " với số tiền: " + depositAmount, e);
                        }
                    } else {
                        log.warn("User không có ví: " + bid.getUser().getEmail());
                    }
                }

                // Gửi email thông báo cho các người thua
                for (String userEmail : losersEmails) {
                    if (!userEmail.equals(winningBid.getUser().getEmail())) {
                        try {
                            emailService.sendResultForAuction(userEmail, winningBid);
                        } catch (Exception e) {
                            log.error("Lỗi khi gửi email cho người dùng: " + userEmail, e);
                        }
                    }
                }

                log.info("Danh sách người dùng được hoàn tiền cho phiên đấu giá " + auction.getAuctionId() + ": "
                        + String.join(", ", refundedUsers));
            }
        }
    }

    // Hàm lấy người thắng (bid cao nhất)
    private Bid getWinningBid(List<Bid> bids) {
        return bids.stream()
                .max(Comparator.comparing(Bid::getBidAmount))
                .orElse(null);
    }

    // Hàm tạo mã giao dịch ngẫu nhiên
    private long random() {
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
