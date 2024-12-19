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
import jakarta.mail.MessagingException;
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

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.second_hand_auction_system.utils.AuctionStatus.CANCELLED;
import static com.second_hand_auction_system.utils.AuctionStatus.CLOSED;

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
    private final OrderRepository orderRepository;
    private final EmailService emailService;
    private final AuctionRegistrationsRepository auctionRegistrationsRepository;

    @Override
    public void addAuction(@Valid AuctionDto auctionDto) throws Exception {
        Item itemExist = itemRepository.findById(auctionDto.getItem())
                .orElseThrow(() -> new Exception("Item not found"));

        AuctionType auctionType = auctionTypeRepository.findById(auctionDto.getAuctionTypeId())
                .orElseThrow(() -> new Exception("Auction type not found"));

        // Kiểm tra loại đấu giá phải khớp với loại của item
        if (!auctionType.getAuctionTypeName().equals(itemExist.getAuctionType().getAuctionTypeName())) {
            throw new Exception("Auction type does not match the item's auction type");
        }

        // Kiểm tra nếu thời gian bắt đầu không sau thời gian kết thúc
        if (auctionDto.getStartDate().after(auctionDto.getEndDate())) {
            throw new Exception("Auction start date cannot be after end date");
        }

        Date currentDate = new Date();
        long diffInMillies = auctionDto.getStartDate().getTime() - currentDate.getTime();
        long diffDays = diffInMillies / (24 * 60 * 60 * 1000);

        // Kiểm tra ngày bắt đầu đấu giá
//        if (diffDays < 1) {
//            throw new Exception("Ngày bắt đầu đấu giá phải cách ít nhất 1 ngày tính từ bây giờ");
//        }
        if (diffDays > 30) {
            throw new Exception("Ngày bắt đầu đấu giá không được cách quá 30 ngày tính từ bây giờ");
        }

        // Kiểm tra giờ bắt đầu không được sau giờ kết thúc
        if (auctionDto.getStartTime().after(auctionDto.getEndTime())) {
            throw new Exception("Start time must be before end time");
        }

        // Kiểm tra giá khởi điểm và giá mua ngay không được âm
        if (auctionDto.getStartPrice() < 0) {
            throw new Exception("Start price must be a non-negative value");
        }
        if (auctionDto.getBuyNowPrice() < 0) {
            throw new Exception("Buy now price must be a non-negative value");
        }

        // Kiểm tra quyền của người dùng
        String authHeader = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
                .getRequest().getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new Exception("Unauthorized");
        }

        String token = authHeader.substring(7);
        String userEmail = jwtService.extractUserEmail(token);
        User requester = userRepository.findByEmailAndStatusIsTrue(userEmail).orElse(null);

        if (requester == null) {
            throw new Exception("User not found");
        }

        // Tạo đối tượng Auction từ DTO và thiết lập các thông tin cần thiết
        Auction auction = modelMapper.map(auctionDto, Auction.class);
        auction.setCreateBy(itemExist.getCreateBy());
        auction.setApproveBy(requester.getFullName());
        auction.setApproveAt(new Date());
        auction.setItem(itemExist);
        auction.setStatus(AuctionStatus.OPEN);
        auction.setAuctionType(auctionType);

        // Tạo và lưu ví cho đấu giá
        Wallet walletAuction = Wallet.builder()
                .balance(0)
                .walletType(WalletType.AUCTION)
                .statusWallet(StatusWallet.ACTIVE)
                .build();

        walletRepository.save(walletAuction);
        auction.setWallet(walletAuction);

        // Cập nhật trạng thái item
        itemExist.setItemStatus(ItemStatus.ACCEPTED);
        itemRepository.save(itemExist);

        // Lưu đấu giá vào cơ sở dữ liệu
        auctionRepository.save(auction);
    }


    @Override
    public void updateAuction(int auctionId, AuctionDto auctionDto) throws Exception {

        //        Item itemExist = itemRepository.findById(auctionDto.getItem())
        //                .orElseThrow(() -> new Exception("Item not found"));
        // Kiểm tra xem Auction tồn tại hay không
        Auction auctionExist = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new Exception("Auction not found"));

        // Cập nhật từng trường nếu chúng được truyền vào
        if (auctionDto.getStartTime() != null) {
            auctionExist.setStartTime(auctionDto.getStartTime());
        }
        if (auctionDto.getEndTime() != null) {
            auctionExist.setEndTime(auctionDto.getEndTime());
        }
        if (auctionDto.getBuyNowPrice() != 0) {
            auctionExist.setBuyNowPrice(auctionDto.getBuyNowPrice());
        }
        if (auctionDto.getStartDate() != null) {
            auctionExist.setStartDate(auctionDto.getStartDate());
        }
        if (auctionDto.getEndDate() != null) {
            auctionExist.setEndDate(auctionDto.getEndDate());
        }
        if (auctionDto.getStartPrice() != 0) {
            auctionExist.setStartPrice(auctionDto.getStartPrice());
        }
        if (auctionDto.getDescription() != null) {
            auctionExist.setDescription(auctionDto.getDescription());
        }
        if (auctionDto.getTermConditions() != null) {
            auctionExist.setTermConditions(auctionDto.getTermConditions());
        }
        if (auctionDto.getPriceStep() != 0) {
            auctionExist.setPriceStep(auctionDto.getPriceStep());
        }
        if (auctionDto.getNumberParticipant() != 0) {
            auctionExist.setNumberParticipant(auctionDto.getNumberParticipant());
        }
        if (auctionDto.getShipType() != null) {
            auctionExist.setShipType(auctionDto.getShipType());
        }
        if (auctionDto.getPercentDeposit() != 0) {
            auctionExist.setPercentDeposit(auctionDto.getPercentDeposit());
        }
        if (auctionDto.getComment() != null) {
            auctionExist.setComment(auctionDto.getComment());
        }

        if (auctionExist.getEndDate() != null && auctionExist.getEndDate().before(new Date())) {
            auctionExist.setStatus(AuctionStatus.CLOSED); // Nếu kết thúc thời gian thì set trạng thái hoàn thành
        } else if (auctionExist.getStartDate() != null && auctionExist.getStartDate().before(new Date())) {
            auctionExist.setStatus(AuctionStatus.OPEN); // Nếu thời gian bắt đầu đã qua thì set OPEN
        } else {
            auctionExist.setStatus(AuctionStatus.PENDING);
        }


        // Lưu thông tin sau khi kiểm tra từng trường
        auctionRepository.save(auctionExist);
    }


    @Override
    public void removeAuction(int auctionId) throws Exception {
        Auction auctionExist = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new Exception("Auction not found"));
        auctionExist.setStatus(CANCELLED);
        auctionRepository.save(auctionExist);
    }

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
        if (auction != null) {
            ResponseAuction responseAuction = ResponseAuction.builder()
                    .itemName(auction.getItem().getItemName())
                    .amount(maxBid)
                    .seller(auction.getCreateBy())
                    .thumbnail(auction.getItem().getThumbnail())
                    .description(auction.getDescription())
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

    @Override
    public long countAuctionsCreatedToday() {
        String authHeader = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest().getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Unauthorized");
        }
        String token = authHeader.substring(7);
        String email = jwtService.extractUserEmail(token);
        if (email == null) {
            throw new RuntimeException("Unauthorized");
        }
        var user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        if (!(user.getRole().equals(Role.ADMIN))) {
            throw new RuntimeException("You don't have permission to access this resource");
        }
        return auctionRepository.countAuctionsCreatedToday();
    }

    @Override
    public ResponseEntity<?> countAuctionsByMonth() {
        String authHeader = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest().getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Unauthorized");
        }
        String token = authHeader.substring(7);
        String email = jwtService.extractUserEmail(token);
        if (email == null) {
            throw new RuntimeException("Unauthorized");
        }
        var user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        if (!(user.getRole().equals(Role.ADMIN))) {
            throw new RuntimeException("You don't have permission to access this resource");
        }
        // Lấy dữ liệu từ repository
        List<Object[]> results = auctionRepository.countAuctionsByMonth();

        // Chuyển đổi dữ liệu sang định dạng JSON-friendly (Map)
        List<Map<String, Object>> response = results.stream().map(row -> {
            Map<String, Object> map = new HashMap<>();
            map.put("month", row[0]);
            map.put("count", row[1]);
            return map;
        }).toList();

        // Trả về kết quả
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<?> updateStatus(Integer auctionId) {
        Auction auction = auctionRepository.findById(auctionId).orElse(null);
        if (auction == null) {
            throw new RuntimeException("Auction not found");
        }
        if (auction.getStatus().equals(AuctionStatus.PENDING) && (auction.getNumberParticipant() >= 2)) {
            auction.setStatus(AuctionStatus.OPEN);
        } else {
            auction.setStatus(AuctionStatus.CLOSED);
        }
        if (auction.getStatus().equals(AuctionStatus.OPEN)) {
            auction.setStatus(AuctionStatus.CLOSED);

        }
        auctionRepository.save(auction);
        return ResponseEntity.ok(ResponseObject.builder()
                .message("Auction updated")
                .data(auction.getStatus())
                .build());
    }


    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void closeExpiredAuctions() throws MessagingException, IOException {
        List<Auction> auctions = auctionRepository.findAll();
        for (Auction auction : auctions) {
            if (auction.getStatus() == null || auction.getStatus().equals(AuctionStatus.CLOSED)) {
                continue;
            }
            LocalDateTime auctionEndTime = auction.getEndDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
                    .with(auction.getEndTime().toLocalTime());
            if (LocalDateTime.now().isAfter(auctionEndTime)) {
                auction.setStatus(AuctionStatus.CLOSED);
                auctionRepository.save(auction);
                log.info("PHIÊN ĐẤU GIÁ KẾT THÚC: " + auction.getAuctionId());

                List<Bid> bids = bidRepository.findByAuction_AuctionId(auction.getAuctionId());
                bids.sort(Comparator.comparing(Bid::getBidAmount).reversed());

                Bid winningBid = !bids.isEmpty() ? bids.get(0) : null;
                Bid secondPlaceBid = bids.size() > 1 ? bids.get(1) : null;

                Wallet walletAuction = auction.getWallet();
                if (walletAuction == null) {
                    log.error("Không tìm thấy ví cọc cho phiên đấu giá: " + auction.getAuctionId());
                    continue;
                }

                if (winningBid != null) {
                    winningBid.setWinBid(true);
                    bidRepository.save(winningBid);
                    log.info("Người thắng đầu tiên: " + winningBid.getUser().getEmail());

                    emailService.sendWinnerNotification(winningBid.getUser().getEmail(), winningBid);

                    // Kiểm tra trạng thái thanh toán qua Order
                    Order order = orderRepository.findByAuction_AuctionId(auction.getAuctionId());
                    if (order != null) {
                        Transaction transaction = transactionRepository.findTransactionByOrder_OrderId(order.getOrderId()).orElse(null);

                        if (transaction == null || !transaction.getTransactionStatus().equals(TransactionStatus.COMPLETED)) {
                            LocalDateTime paymentDeadline = auctionEndTime.plusHours(24);

                            if (LocalDateTime.now().isAfter(paymentDeadline)) {
                                // Thêm tiền cọc vào ví admin
                                Wallet adminWallet = walletRepository.findWalletByWalletType(WalletType.ADMIN)
                                        .orElseThrow(() -> new IllegalStateException("Không tìm thấy ví admin"));
                                double depositAmount = (auction.getPercentDeposit() * auction.getBuyNowPrice()) / 100;
                                adminWallet.setBalance(adminWallet.getBalance() + depositAmount);
                                walletRepository.save(adminWallet);
                                log.info("Tiền cọc được chuyển vào ví admin: " + depositAmount);

                                // Hoàn tiền cho người thua
                                List<Bid> losingBids = bidRepository.findByAuction_AuctionIdAndWinBidFalse(auction.getAuctionId());
                                for (Bid losingBid : losingBids) {
                                    Wallet userWallet = walletRepository.findWalletByUserId(losingBid.getUser().getId()).orElse(null);
                                    double refundAmount = ((losingBid.getAuction().getPercentDeposit() * auction.getBuyNowPrice()) / 100);
                                    assert userWallet != null;
                                    userWallet.setBalance(userWallet.getBalance() + refundAmount);
                                    walletRepository.save(userWallet);
                                    Transaction transactionRefund = Transaction.builder()
                                            .recipient(userWallet.getUser().getFullName())
                                            .commissionRate(0)
                                            .description("Hoàn tiền sau phien đấu giá")
                                            .commissionAmount(0)
                                            .transactionStatus(TransactionStatus.COMPLETED)
                                            .sender("Hệ thống phiên đấu giá" + auction.getAuctionId())
                                            .wallet(userWallet)
                                            .build();
                                    transactionRepository.save(transactionRefund);
                                    log.info("Hoàn tiền cọc: " + refundAmount + " vào ví của người dùng: " + losingBid.getUser().getEmail());
                                    emailService.sendResultForAuction(losingBid.getUser().getEmail(), winningBid);
                                }
                                if (secondPlaceBid != null) {
                                    secondPlaceBid.setWinBid(true);
                                    bidRepository.save(secondPlaceBid);

                                    emailService.sendWinnerNotification(secondPlaceBid.getUser().getEmail(), secondPlaceBid);
                                    log.info("Người thắng thứ hai: " + secondPlaceBid.getUser().getEmail());
                                } else {
                                    auction.setStatus(AuctionStatus.CANCELLED);
                                    auctionRepository.save(auction);
                                    log.info("Phiên đấu giá thất bại do không có người thanh toán.");
                                }
                            } else {
                                log.info("Người thắng đầu tiên vẫn còn thời gian để thanh toán: " + auction.getAuctionId());
                            }
                        }
                    } else {
                        auction.setStatus(AuctionStatus.CANCELLED);
                        auctionRepository.save(auction);
                        log.info("Phiên đấu giá thất bại do không có người đặt giá.");
                    }
                }
            }
        }
    }


    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void openAuction() throws Exception {
        List<Auction> auctions = auctionRepository.findAll();
        for (Auction auction : auctions) {
            // Nếu trạng thái chưa được xác định hoặc đã là OPEN thì bỏ qua
            if (auction.getStatus() == null || auction.getStatus().equals(AuctionStatus.OPEN)) {
                continue;
            }
            // Lấy thời gian bắt đầu của đấu giá từ startDate và startTime
            LocalDateTime auctionStartTime = auction.getStartDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
                    .with(auction.getStartTime().toLocalTime());
            // Kiểm tra nếu thời gian hiện tại đã qua thời gian bắt đầu của đấu giá
            if (LocalDateTime.now().isAfter(auctionStartTime)) {
                auction.setStatus(AuctionStatus.OPEN);
                auctionRepository.save(auction);
            }
        }
    }


    private Bid getWinningBid(List<Bid> bids) {
        return bids.stream()
                .max(Comparator.comparing(Bid::getBidAmount))
                .orElse(null);
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
