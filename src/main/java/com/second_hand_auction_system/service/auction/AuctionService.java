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
import java.sql.Time;
import java.sql.Timestamp;
import java.time.*;
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
                .orElseThrow(() -> new Exception("Sản phẩm không tìm thấy"));

        AuctionType auctionType = auctionTypeRepository.findById(auctionDto.getAuctionTypeId())
                .orElseThrow(() -> new Exception("Phiên đấu giá không tìm thấy"));

        // Kiểm tra loại đấu giá phải khớp với loại của item
        if (!auctionType.getAuctionTypeName().equals(itemExist.getAuctionType().getAuctionTypeName())) {
            throw new Exception("Loại đấu giá không khớp với loại đấu giá của mặt hàng");
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
            throw new Exception("Thời gian bắt đầu phải trước thời gian kết thúc");
        }

        // Kiểm tra giá khởi điểm và giá mua ngay không được âm
        if (auctionDto.getStartPrice() < 0) {
            throw new Exception("Giá khởi điểm phải là giá trị không âm");
        }
        if (auctionDto.getBuyNowPrice() < 0) {
            throw new Exception("Giá mua ngay phải là giá trị không âm");
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
            throw new Exception("Không tìm thấy người dùng");
        }

        // Tạo đối tượng Auction từ DTO và thiết lập các thông tin cần thiết
        Auction auction = modelMapper.map(auctionDto, Auction.class);
        auction.setCreateBy(itemExist.getCreateBy());
        auction.setApproveBy(requester.getFullName());
        auction.setApproveAt(new Date());
        auction.setItem(itemExist);
        auction.setStatus(AuctionStatus.PENDING);
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
                .orElseThrow(() -> new Exception("Phiên đấu giá không tìm thấy"));

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

        Date currentDateTime = new Date(); // Lấy thời gian hiện tại

// Kiểm tra nếu đấu giá đã kết thúc
        if (auctionExist.getEndDate() != null && auctionExist.getEndTime() != null &&
                combineDateAndTime(auctionExist.getEndDate(), auctionExist.getEndTime()).before(currentDateTime)) {
            auctionExist.setStatus(AuctionStatus.CLOSED);
        }
// Kiểm tra nếu đấu giá đang mở
        else if (auctionExist.getStartDate() != null && auctionExist.getStartTime() != null &&
                combineDateAndTime(auctionExist.getStartDate(), auctionExist.getStartTime()).before(currentDateTime)) {
            auctionExist.setStatus(AuctionStatus.OPEN);
        }
// Nếu chưa bắt đầu, đặt trạng thái là PENDING
        else {
            auctionExist.setStatus(AuctionStatus.PENDING);
        }


        // Lưu thông tin sau khi kiểm tra từng trường
        auctionRepository.save(auctionExist);
    }

    private Date combineDateAndTime(Date date, Date time) {
        Calendar calendar = Calendar.getInstance();

        // Set ngày
        Calendar datePart = Calendar.getInstance();
        datePart.setTime(date);

        // Set giờ
        Calendar timePart = Calendar.getInstance();
        timePart.setTime(time);

        calendar.set(Calendar.YEAR, datePart.get(Calendar.YEAR));
        calendar.set(Calendar.MONTH, datePart.get(Calendar.MONTH));
        calendar.set(Calendar.DAY_OF_MONTH, datePart.get(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, timePart.get(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, timePart.get(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, timePart.get(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    @Override
    public void removeAuction(int auctionId) throws Exception {
        Auction auctionExist = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new Exception("Phiên đấu giá không tìm thấy"));
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
            throw new RuntimeException("Không tìm thấy người dùng");
        }
        if (!(user.getRole().equals(Role.ADMIN))) {
            throw new RuntimeException("Bạn không có quyền truy cập vào tài nguyên này");
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
            throw new RuntimeException("Không tìm thấy người dùng");
        }
        if (!(user.getRole().equals(Role.ADMIN))) {
            throw new RuntimeException("Bạn không có quyền truy cập vào tài nguyên này");
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
            throw new RuntimeException("Phiên đấu giá không tìm thấy");
        }

        if (auction.getStatus().equals(AuctionStatus.PENDING)) {
            auction.setStatus(AuctionStatus.OPEN);
        }
//        if (auction.getStatus().equals(AuctionStatus.OPEN)) {
//            if (auction.getEndTime().before(new Timestamp(System.currentTimeMillis()))) {
//                auction.setStatus(AuctionStatus.CLOSED);
//            }
//        }

        // Lưu lại thay đổi trong cơ sở dữ liệu
        auctionRepository.save(auction);

        return ResponseEntity.ok(ResponseObject.builder()
                .message("Phiên đấu giá đã cập nhật")
                .data(auction.getStatus())
                .build());
    }


    @Scheduled(fixedRate = 60000, zone = "Asia/Ho_Chi_Minh")
    @Transactional
    public void processAuctionCompletion() {
        ZoneId systemZoneId = ZoneId.of("Asia/Ho_Chi_Minh");
        // Kiểm tra các phiên đấu giá có trạng thái CLOSED hoặc AWAITING_PAYMENT
        List<Auction> auctionsToProcess = auctionRepository.findByStatusIn(Arrays.asList(AuctionStatus.CLOSED, AuctionStatus.AWAITING_PAYMENT));

        for (Auction auction : auctionsToProcess) {
            try {
                // Kiểm tra thời gian kết thúc của phiên đấu giá
                ZonedDateTime auctionEndTime = ZonedDateTime.of(
                        auction.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                        auction.getEndTime().toLocalTime(),
                        ZoneId.of("Asia/Ho_Chi_Minh")
                );
                if (ZonedDateTime.now(systemZoneId).isAfter(auctionEndTime)) {
                    log.info("Đang xử lý phiên đấu giá ID: {}", auction.getAuctionId());
                    // Lấy danh sách tất cả người tham gia bid (có thể đặt nhiều bid)
                    List<User> participants = bidRepository.findDistinctUsersByAuction_AuctionId(auction.getAuctionId());
                    if (participants.isEmpty()) {
                        log.warn("Phiên đấu giá không có bid nào, không cần xử lý.");
                        continue;
                    }
                    // Lấy bid thắng cuộc (bid cao nhất)
                    Bid winningBid = bidRepository.findTopByAuction_AuctionIdOrderByBidAmountDesc(auction.getAuctionId());
                    assert winningBid != null;

                    // Hoàn tiền cho người thua
                    for (User participant : participants) {
                        if (!participant.equals(winningBid.getUser())) {
                            processDepositRefund(participant, auction); // Hoàn cọc cho người thua
                        }
                    }

                    // Kiểm tra và hoàn tiền cho người thắng cuộc (nếu đã thanh toán)
                    processWinnerDepositRefund(winningBid, auction);
                    auctionRepository.save(auction);
                    log.info("Đã chuyển trạng thái phiên đấu giá ID: {} sang AWAITING_PAYMENT.", auction.getAuctionId());
                }
            } catch (Exception e) {
                log.error("Lỗi khi xử lý phiên đấu giá ID: " + auction.getAuctionId(), e);
            }
        }
    }





    private void processWinnerDepositRefund(Bid winningBid, Auction auction) {
        try {
            User winner = winningBid.getUser();
            Wallet winnerWallet = walletRepository.findWalletByUserId(winner.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy ví cho user: " + winner.getEmail()));

            // Kiểm tra nếu đã có giao dịch hoàn cọc với trạng thái COMPLETED
            Optional<Transaction> existingRefund = transactionRepository.findByWalletAndTransactionTypeAndTransactionStatusAndAuction(
                    winnerWallet, TransactionType.REFUND, TransactionStatus.COMPLETED, auction);

            if (existingRefund.isPresent()) {
                log.info("Đã có giao dịch hoàn cọc cho user: " + winner.getEmail() + ", sẽ không thực hiện lại.");
                return; // Bỏ qua nếu đã hoàn cọc cho người này
            }

            // Kiểm tra trạng thái thanh toán của người thắng cuộc
            boolean isPaymentCompleted = orderRepository.existsByAuctionAndUser(auction, winner);

            if (!isPaymentCompleted) {
                log.warn("Người thắng cuộc chưa thanh toán thành công cho phiên đấu giá ID: {}", auction.getAuctionId());
                return; // Không hoàn tiền nếu chưa thanh toán
            }

            // Xác định số tiền đặt cọc
            double depositAmount = (auction.getPercentDeposit() * auction.getBuyNowPrice()) / 100;

            // Cộng tiền cọc vào ví người dùng
            winnerWallet.setBalance(winnerWallet.getBalance() + depositAmount);
            walletRepository.save(winnerWallet);

            // Tạo giao dịch hoàn tiền
            long oldBalance = (long) (winnerWallet.getBalance() - depositAmount); // Số dư trước khi hoàn tiền
            long newBalance = (long) winnerWallet.getBalance(); // Số dư sau khi hoàn tiền

            Transaction refundTransaction = Transaction.builder()
                    .wallet(winnerWallet)
                    .transactionStatus(TransactionStatus.COMPLETED)
                    .description("Hoàn cọc cho người thắng cuộc trong phiên đấu giá " + auction.getAuctionId())
                    .transactionType(TransactionType.REFUND)
                    .recipient(winnerWallet.getUser().getFullName())
                    .sender("SYSTEM")
                    .transactionWalletCode(random())
                    .oldAmount(oldBalance) // Số dư trước khi hoàn tiền
                    .netAmount(newBalance) // Số dư sau khi hoàn tiền
                    .amount((long) depositAmount) // Giá trị số tiền hoàn cọc
                    .build();

            // Lưu giao dịch hoàn cọc vào cơ sở dữ liệu
            transactionRepository.save(refundTransaction);
            auction.setStatus(AuctionStatus.COMPLETED);
            auctionRepository.save(auction);
            log.info("Hoàn cọc cho user: {}, Số tiền: {}", winnerWallet.getUser().getEmail(), depositAmount);
            emailService.sendResultForAuction(winnerWallet.getUser().getEmail(), null);

        } catch (Exception ex) {
            log.error("Lỗi khi xử lý hoàn cọc cho user ID: " + winningBid.getUser().getId() + " trong phiên đấu giá ID: " + auction.getAuctionId(), ex);
        }
    }


    private void processDepositRefund(User user, Auction auction) {
        try {
            // Kiểm tra trạng thái của phiên đấu giá, chỉ tiếp tục nếu trạng thái là CLOSED
            if (!auction.getStatus().equals(AuctionStatus.CLOSED)) {
                log.info("Phiên đấu giá ID: {} không phải trạng thái CLOSED, không thực hiện hoàn cọc.", auction.getAuctionId());
                return; // Nếu không phải trạng thái CLOSED, kết thúc
            }

            // Lấy ví của người dùng
            Wallet userWallet = walletRepository.findWalletByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy ví cho user: " + user.getEmail()));

            // Kiểm tra nếu đã có giao dịch hoàn cọc với trạng thái COMPLETED
            Optional<Transaction> existingRefund = transactionRepository.findByWalletAndTransactionTypeAndTransactionStatusAndAuction(
                    userWallet, TransactionType.REFUND, TransactionStatus.COMPLETED, auction);

            if (existingRefund.isPresent()) {
                log.info("Đã có giao dịch hoàn cọc cho user: " + user.getEmail() + ", sẽ không thực hiện lại.");
                return; // Bỏ qua nếu đã hoàn cọc cho người này
            }

            // Xác định số tiền đặt cọc
            double depositAmount = (auction.getPercentDeposit() * auction.getBuyNowPrice()) / 100;

            // Cộng tiền cọc vào ví người dùng
            userWallet.setBalance(userWallet.getBalance() + depositAmount);
            walletRepository.save(userWallet);

            // Tạo giao dịch hoàn tiền
            long oldBalance = (long) (userWallet.getBalance() - depositAmount); // Số dư trước khi hoàn tiền
            long newBalance = (long) userWallet.getBalance(); // Số dư sau khi hoàn tiền

            Transaction refundTransaction = Transaction.builder()
                    .wallet(userWallet)
                    .transactionStatus(TransactionStatus.COMPLETED)
                    .description("Hoàn cọc cho người thua cuộc trong phiên đấu giá " + auction.getAuctionId())
                    .transactionType(TransactionType.REFUND)
                    .recipient(userWallet.getUser().getFullName())
                    .sender("SYSTEM")
                    .transactionWalletCode(random())
                    .oldAmount(oldBalance) // Số dư trước khi hoàn tiền
                    .netAmount(newBalance) // Số dư sau khi hoàn tiền
                    .amount((long) depositAmount) // Giá trị số tiền hoàn cọc
                    .build();

            // Lưu giao dịch hoàn cọc vào cơ sở dữ liệu
            transactionRepository.save(refundTransaction);
            auction.setStatus(AuctionStatus.AWAITING_PAYMENT);
            auctionRepository.save(auction);
            log.info("Hoàn cọc cho user: {}, Số tiền: {}", userWallet.getUser().getEmail(), depositAmount);
            emailService.sendResultForAuction(userWallet.getUser().getEmail(), null);

        } catch (Exception ex) {
            log.error("Lỗi khi xử lý hoàn cọc cho user ID: " + user.getId() + " trong phiên đấu giá ID: " + auction.getAuctionId(), ex);
        }
    }



    private long random() {
        return (long) (Math.random() * 900000) + 100000; // Tạo số trong khoảng [100000, 999999]
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
