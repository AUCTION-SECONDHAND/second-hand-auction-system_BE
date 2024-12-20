package com.second_hand_auction_system.service.bid;

import com.second_hand_auction_system.converters.bid.BidConverter;
import com.second_hand_auction_system.converters.notification.NotificationConverter;
import com.second_hand_auction_system.dtos.request.bid.BidDto;
import com.second_hand_auction_system.dtos.request.bid.BidRequest;
import com.second_hand_auction_system.dtos.responses.ResponseObject;
import com.second_hand_auction_system.dtos.responses.auction.AuctionResponse;
import com.second_hand_auction_system.dtos.responses.bid.BidDetailResponse;
import com.second_hand_auction_system.dtos.responses.bid.BidInformation;
import com.second_hand_auction_system.dtos.responses.bid.BidResponse;
import com.second_hand_auction_system.dtos.responses.notification.NotificationResponse;
import com.second_hand_auction_system.models.*;
import com.second_hand_auction_system.repositories.*;
import com.second_hand_auction_system.service.email.EmailService;
import com.second_hand_auction_system.service.jwt.IJwtService;
import com.second_hand_auction_system.service.notification.INotificationService;
import com.second_hand_auction_system.sse.NotificationEvent;
import com.second_hand_auction_system.utils.AuctionStatus;
import com.second_hand_auction_system.utils.Registration;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.sql.Time;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BidService implements IBidService {
    private final BidRepository bidRepository;
    private final IJwtService jwtService;
    private final UserRepository userRepository;
    private final AuctionRegistrationsRepository auctionRegistrationsRepository;
    private final AuctionRepository auctionRepository;
    private final EmailService emailService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ModelMapper modelMapper;
    private final List<SseEmitter> emitters = Collections.synchronizedList(new ArrayList<>());
    private final ApplicationEventPublisher applicationEventPublisher;
    private final INotificationService notificationService;
    private final NotificationsRepository notificationsRepository;
    private final NotificationConverter notificationConverter;

    public void addEmitter(SseEmitter emitter) {
        emitters.add(emitter);
    }

    public void removeEmitter(SseEmitter emitter) {
        emitters.remove(emitter);
    }

    @Override
    @Transactional
    public ResponseEntity<?> createBid(BidRequest bidRequest) throws Exception {
        // 1. Lấy token từ header Authorization
        String token = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
                .getRequest().getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ResponseObject.builder()
                            .data(null)
                            .message("Unauthorized")
                            .status(HttpStatus.UNAUTHORIZED)
                            .build());
        }

        // 2. Trích xuất email từ token
        token = token.substring(7); // Loại bỏ "Bearer "
        String email = jwtService.extractUserEmail(token);
        User requester = userRepository.findByEmail(email).orElse(null);
        if (requester == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .data(null)
                            .message("User not found")
                            .status(HttpStatus.NOT_FOUND)
                            .build());
        }

        // 3. Lấy thông tin phiên đấu giá
        Auction auction = auctionRepository.findById(bidRequest.getAuctionId()).orElse(null);
        if (auction == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .data(null)
                            .message("Auction not found")
                            .status(HttpStatus.NOT_FOUND)
                            .build());
        }

        // 4. Kiểm tra trạng thái và thời gian của phiên đấu giá
        if (!AuctionStatus.OPEN.equals(auction.getStatus())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .data(null)
                            .message("Auction is not currently open")
                            .status(HttpStatus.BAD_REQUEST)
                            .build());
        }

        // 5. Kiểm tra người dùng đã đăng ký tham gia đấu giá
        boolean isRegistered = auctionRegistrationsRepository
                .existsAuctionRegistrationByUserIdAndAuctionIdAndRegistrationTrue(requester.getId(), auction.getAuctionId());
        if (!isRegistered) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .data(null)
                            .message("Bạn chưa đăng ký tham gia đấu giá")
                            .status(HttpStatus.BAD_REQUEST)
                            .build());
        }

        // 6. Kiểm tra giá thầu hiện tại và xử lý "Buy Now"
        double bidAmount = bidRequest.getBidAmount();
        Bid highestBid = bidRepository.findTopByAuction_AuctionIdOrderByBidAmountDesc(auction.getAuctionId());

        // 6.1. Kiểm tra điều kiện đặt giá
        double minBid = (highestBid == null) ? auction.getStartPrice() : highestBid.getBidAmount() + auction.getPriceStep();
        if (bidAmount < minBid) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .data(null)
                            .message("Giá đặt phải lớn hơn mức giá hiện tại và bước giá quy định.")
                            .status(HttpStatus.BAD_REQUEST)
                            .build());
        }

        if (highestBid != null && highestBid.getUser().getId().equals(requester.getId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .data(null)
                            .message("Bạn là người đặt giá cao nhất hiện tại, không thể đặt thêm giá.")
                            .status(HttpStatus.BAD_REQUEST)
                            .build());
        }

        // 6.2. Xử lý "Buy Now"
        if (bidAmount == auction.getBuyNowPrice()) {
            if (highestBid != null && highestBid.getBidAmount() >= auction.getBuyNowPrice()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        ResponseObject.builder()
                                .data(null)
                                .message("Giá thầu hiện tại đã cao hơn hoặc bằng giá mua ngay.")
                                .status(HttpStatus.BAD_REQUEST)
                                .build());
            }

            // Kiểm tra thời gian kết thúc của phiên đấu giá
            // Get current time and auction end time
            LocalDateTime nowDateTime = LocalDateTime.now();
            LocalDateTime auctionEndDateTime = LocalDateTime.of(auction.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), auction.getEndTime().toLocalTime());

            // Calculate the remaining time in minutes
            long minutesRemaining = ChronoUnit.MINUTES.between(nowDateTime, auctionEndDateTime);

            if (minutesRemaining > 10) {
                // Nếu còn hơn 10 phút, chỉnh lại thời gian kết thúc là 10 phút từ bây giờ
                LocalDateTime newEndDateTime = nowDateTime.plusMinutes(10); // Add 10 minutes to the current time
                auction.setEndDate(Date.from(newEndDateTime.atZone(ZoneId.systemDefault()).toInstant())); // Update the auction end date
                auction.setEndTime(Time.valueOf(newEndDateTime.toLocalTime())); // Update the auction end time to 10 minutes from now
            } else {
                // Nếu còn ít hơn hoặc bằng 10 phút, giữ nguyên thời gian kết thúc
                auction.setEndDate(auction.getEndDate());  // No change
                auction.setEndTime(auction.getEndTime());  // No change
            }

            // Save the updated auction
            auctionRepository.save(auction);

            // Đánh dấu tất cả các bid hiện tại là không thắng
            bidRepository.findByAuction_AuctionIdOrderByBidAmountDesc(auction.getAuctionId())
                    .forEach(b -> {
                        b.setWinBid(false);
                        bidRepository.save(b);
                    });

            // Tạo bid "mua ngay" và cập nhật trạng thái đấu giá
            Bid buyNowBid = Bid.builder()
                    .winBid(true)
                    .bidTime(LocalDateTime.now())
                    .auction(auction)
                    .user(requester)
                    .bidAmount((int) bidAmount)
                    .build();
            bidRepository.save(buyNowBid);

            auctionRepository.save(auction);

            // Gửi thông báo và cập nhật frontend
            notificationService.createBidNotification(requester.getId(), "Đấu giá kết thúc",
                    "Bạn đã mua ngay thành công với giá: " + auction.getBuyNowPrice());

            messagingTemplate.convertAndSend("/topic/bids", "Auction ended with buy now price: " + auction.getBuyNowPrice());

            return ResponseEntity.status(HttpStatus.OK).body(
                    ResponseObject.builder()
                            .data(null)
                            .message("Bạn đã mua ngay thành công với giá: " + auction.getBuyNowPrice())
                            .status(HttpStatus.OK)
                            .build());
        }






        // 6.3. Xử lý đặt bid thông thường
        Bid newBid = Bid.builder()
                .winBid(true)
                .bidTime(LocalDateTime.now())
                .auction(auction)
                .user(requester)
                .bidAmount((int) bidAmount)
                .build();
        bidRepository.save(newBid);

        BidResponse bidResponse = BidResponse.builder()
                .bidAmount(newBid.getBidAmount())
                .bidTime(newBid.getBidTime())
                .winBid(newBid.isWinBid())
                .userId(newBid.getUser().getId())
                .auctionId(newBid.getAuction().getAuctionId())
                .username(newBid.getUser().getFullName())
                .build();

        sendBidUpdate(bidResponse);
        // Cập nhật bid cao nhất trước đó thành không thắng
        if (highestBid != null) {
            highestBid.setWinBid(false);
            bidRepository.save(highestBid);
        }

        // Thông báo đặt giá thành công
        notificationService.createBidNotification(requester.getId(), "Đặt giá thành công",
                "Bạn đã đặt giá: " + bidAmount);

        messagingTemplate.convertAndSend("/topic/bids", "New bid placed: " + bidAmount);

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseObject.builder()
                        .data(null)
                        .message("Đặt giá thành công.")
                        .status(HttpStatus.OK)
                        .build());
    }


    @Override
    public ResponseEntity<?> createBidSealedBid(BidRequest bidRequest) throws Exception {
        String token = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
                .getRequest().getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ResponseObject.builder()
                            .data(null)
                            .message("Unauthorized")
                            .status(HttpStatus.UNAUTHORIZED)
                            .build());
        }

        token = token.substring(7);
        String email = jwtService.extractUserEmail(token);
        User requester = userRepository.findByEmail(email).orElse(null);

        if (requester == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .data(null)
                            .message("User not found")
                            .status(HttpStatus.NOT_FOUND)
                            .build());
        }

        Auction auction = auctionRepository.findById(bidRequest.getAuctionId()).orElse(null);
        if (auction == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .data(null)
                            .message("Auction not found")
                            .status(HttpStatus.NOT_FOUND)
                            .build());
        }

        // Kiểm tra nếu người dùng đã đặt giá trong phiên này chưa
        boolean alreadyBid = bidRepository.findByAuction_AuctionIdAndUser_Id(
                auction.getAuctionId(), requester.getId()).isPresent();

        if (alreadyBid) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .data(null)
                            .message("Bạn đã từng đặt giá trong phiên đấu giá này rồi")
                            .status(HttpStatus.BAD_REQUEST)
                            .build());
        }

        List<Bid> existingBids = bidRepository.findByAuction_AuctionIdOrderByBidAmountDesc(auction.getAuctionId());

        // Bid mới luôn được tạo với winBid=false mặc định
        Bid newBid = Bid.builder()
                .winBid(false) // Chưa xác định bid mới có thể thắng
                .bidTime(LocalDateTime.now())
                .auction(auction)
                .user(requester)
                .bidAmount(bidRequest.getBidAmount())
                .build();

        // Nếu bid mới thực sự là giá cao nhất, gán winBid=true
        if (existingBids.isEmpty() || bidRequest.getBidAmount() > existingBids.get(0).getBidAmount()) {
            newBid.setWinBid(true);

            // set winBid = false
            for (Bid bid : existingBids) {
                if (!bid.getBidId().equals(newBid.getBidId())) {
                    bid.setWinBid(false);
                }
            }
        }
        // Duy trì trạng thái của các bid cũ, không sửa trừ khi là không hợp lệ
        bidRepository.saveAll(existingBids);
        bidRepository.save(newBid);
        // Gửi thông báo
        String message = "Bạn đang đặt với giá " + newBid.getBidAmount();
        String title = "Đặt giá thành công";
        notificationService.createBidNotification(requester.getId(), title, message);

        List<Notifications> notifications = notificationsRepository.findByUser_IdOrderByCreateAtDesc(requester.getId());
        List<NotificationResponse> notificationResponses = notifications.stream()
                .map(notificationConverter::toNotificationResponse)
                .toList();
        applicationEventPublisher.publishEvent(new NotificationEvent(notificationResponses));

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseObject.builder()
                        .data(Map.of(
                                "bidAmount", newBid.getBidAmount(),
                                "isHighestBid", newBid.isWinBid()))
                        .message("Bid placed successfully")
                        .status(HttpStatus.OK)
                        .build());
    }


    @Override
    public ResponseEntity<?> updateBid(Integer bidId, BidRequest bidDto) {
        try {
            // 1. Extract token from the request
            String token = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
                    .getRequest().getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                return buildUnauthorizedResponse();
            }

            token = token.substring(7); // Remove "Bearer "
            String email = jwtService.extractUserEmail(token);

            // 2. Validate user
            User requester = userRepository.findByEmail(email).orElse(null);
            if (requester == null) {
                return buildNotFoundResponse("User not found");
            }

            // 3. Check if the bid exists
            Bid existingBid = bidRepository.findById(bidId).orElse(null);
            if (existingBid == null) {
                return buildNotFoundResponse("Bạn chưa dua ra muc gia nao");
            }

            // 4. Find the auction
            Auction auction = auctionRepository.findById(bidDto.getAuctionId()).orElse(null);
            if (auction == null) {
                return buildNotFoundResponse("Auction not found");
            }

            // 5. Check auction type
            if (!isEnglishAuction(auction)) {
                return buildBadRequestResponse("Không thể đặt giá bid cho đấu giá ngược");
            }

            // 6. Validate new bid amount
            if (bidDto.getBidAmount() <= existingBid.getBidAmount()) {
                return buildBadRequestResponse("New bid amount must be higher than the existing bid amount: " + existingBid.getBidAmount());
            }

            // 7. Check auction timing
            if (!isAuctionActive(auction)) {
                return buildBadRequestResponse("Bidding is not allowed at this time");
            }

            // 8. Check auction status
            if (!auction.getStatus().equals(AuctionStatus.OPEN)) {
                return buildBadRequestResponse("Auction is not open for bidding");
            }

            // 9. Update the bid
            existingBid.setBidAmount(bidDto.getBidAmount());
            existingBid.setBidTime(LocalDateTime.now());
            List<Bid> bids = bidRepository.findByAuction_AuctionId(auction.getAuctionId());
            for (Bid bid : bids) {
                bid.setWinBid(false); // Set all bids to not win
            }

            Bid highestBid = bids.stream()
                    .max(Comparator.comparingInt(Bid::getBidAmount))
                    .orElse(null);

            if (highestBid != null) {
                highestBid.setWinBid(true); // Set the highest bid to win
            }
            Bid updatedBid = bidRepository.save(existingBid);
            BidResponse bidResponse = BidResponse.builder()
                    .winBid(true)
                    .bidAmount(bidDto.getBidAmount())
                    .bidTime(LocalDateTime.now())
                    .userId(requester.getId())
                    .auctionId(auction.getAuctionId())
                    .username(requester.getUsername())
                    .build();
            emailService.sendBidNotification(email, requester.getFullName(), bidDto.getBidAmount(), existingBid.getBidAmount(), auction.getAuctionId());
            // 10. Return success response
            return ResponseEntity.ok(ResponseObject.builder()
                    .data(bidResponse)
                    .message("Bid updated successfully")
                    .status(HttpStatus.OK)
                    .build());
        } catch (Exception e) {
            // Handle exceptions
            return buildErrorResponse(e);
        }
    }

    // Helper methods for building responses
    private ResponseEntity<ResponseObject> buildUnauthorizedResponse() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ResponseObject.builder()
                        .data(null)
                        .message("Unauthorized")
                        .status(HttpStatus.UNAUTHORIZED)
                        .build());
    }

    private ResponseEntity<ResponseObject> buildNotFoundResponse(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ResponseObject.builder()
                        .data(null)
                        .message(message)
                        .status(HttpStatus.NOT_FOUND)
                        .build());
    }

    private ResponseEntity<ResponseObject> buildBadRequestResponse(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ResponseObject.builder()
                        .data(null)
                        .message(message)
                        .status(HttpStatus.BAD_REQUEST)
                        .build());
    }

    private ResponseEntity<ResponseObject> buildErrorResponse(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ResponseObject.builder()
                        .data(null)
                        .message("Error occurred: " + e.getMessage())
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .build());
    }

    private boolean isEnglishAuction(Auction auction) {
        return auction.getAuctionType().getAuctionTypeName().equals("Đấu giá kiểu Anh");
    }

    private boolean isAuctionActive(Auction auction) {
        LocalDateTime auctionStartDateTime = auction.getStartDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .with(auction.getStartTime().toLocalTime());
        LocalDateTime auctionEndDateTime = auction.getEndDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .with(auction.getEndTime().toLocalTime());
        return !(LocalDateTime.now().isBefore(auctionStartDateTime) || LocalDateTime.now().isAfter(auctionEndDateTime));
    }


    @Override
    public ResponseEntity<?> deleteBid(Integer bidId) throws Exception {
        var bid = bidRepository.findById(bidId).orElse(null);
        if (bid != null) {
            bidRepository.delete(bid);
            return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder()
                    .status(HttpStatus.OK)
                    .message("Deleted successfully")
                    .data(null)
                    .build());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                .status(HttpStatus.NOT_FOUND)
                .message("Bid not found")
                .data(null)
                .build());
    }

    @Override
    public BidResponse getBidById(Integer bidId) throws Exception {
        // Find bid
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new Exception("Bid not found"));

        // Convert entity to response
        return BidConverter.convertToResponse(bid);
    }

    @Override
    public List<BidResponse> getAllBidsByAuctionId(Integer auctionId) throws Exception {
        // Find all bids by auction
        List<Bid> bids = bidRepository.findByAuction_AuctionId(auctionId);

        // Convert list of entities to list of responses
        return bids.stream()
                .map(BidConverter::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ResponseEntity<?> findWinnerAuction(int auctionId) {
        // Lấy token từ header Authorization
        String token = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
                .getRequest().getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ResponseObject.builder()
                            .data(null)
                            .message("Unauthorized")
                            .status(HttpStatus.UNAUTHORIZED)
                            .build());
        }
        token = token.substring(7); // Lấy token sau "Bearer "
        String email = jwtService.extractUserEmail(token);
        User requester = userRepository.findByEmail(email).orElse(null);
        if (requester == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .data(null)
                            .message("User not found")
                            .status(HttpStatus.NOT_FOUND)
                            .build());
        }

        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("Auction not found"));
        List<Bid> bids = bidRepository.findByAuction_AuctionId(auctionId);
        if (bids.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .data(null)
                            .message("No bids found for this auction")
                            .status(HttpStatus.NOT_FOUND)
                            .build());
        }

        // Tìm ra bid thắng cuộc (giá cao nhất)
        Bid winningBid = bids.stream()
                .max(Comparator.comparingInt(Bid::getBidAmount))
                .orElse(null);

        // Kiểm tra xem người dùng hiện tại có đặt bid trong phiên đấu giá này không
        List<Bid> userBids = bids.stream()
                .filter(bid -> Objects.equals(bid.getUser().getId(), requester.getId()))
                .collect(Collectors.toList());

        if (!userBids.isEmpty()) {
            // Số lượng bid của người dùng
            int userBidCount = userBids.size();

            // Bid của người dùng với giá cao nhất
            Bid userHighestBid = userBids.stream()
                    .max(Comparator.comparingInt(Bid::getBidAmount))
                    .orElse(null);

            // Kiểm tra xem bid cao nhất của người dùng có phải là bid thắng không
            boolean isWinner = userHighestBid != null && userHighestBid.getBidAmount().equals(winningBid.getBidAmount());

            // Trả về thông tin với số lượng bid, số tiền bid cao nhất và trạng thái thắng thua của người dùng
            return ResponseEntity.status(HttpStatus.OK).body(
                    ResponseObject.builder()
                            .status(HttpStatus.OK)
                            .message(isWinner ? "You have won the auction" : "You did not win the auction")
                            .data(BidResponse.builder()
                                    .bidAmount(winningBid.getBidAmount())
                                    .bidTime(userHighestBid.getBidTime())
                                    .winBid(isWinner)
                                    .userId(userHighestBid.getUser().getId())
                                    .auctionId(auctionId)
                                    .username(userHighestBid.getUser().getFullName())
                                    .build())
                            .build());
        }

        // Nếu người dùng không có bid trong phiên đấu giá này
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ResponseObject.builder()
                        .status(HttpStatus.NOT_FOUND)
                        .message("You did not participate in this auction")
                        .data(null)
                        .build());
    }


    @Override
    public ResponseEntity<?> getAllBids(Integer auctionId, int limit, int page) {
        Pageable pageable = PageRequest.of(page, limit);
        var auction = auctionRepository.findById(auctionId).orElse(null);
        if (auction == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "status", HttpStatus.NOT_FOUND,
                    "message", "Auction not found",
                    "data", null
            ));
        }

        Page<Bid> bidPage = bidRepository.findAllByAuction_AuctionIdOrderByBidAmountDesc(auctionId, pageable);
        List<BidResponse> bidResponses = new ArrayList<>();

        // Giả định rằng các giá thầu được sắp xếp theo thứ tự giảm dần và giá thầu đầu tiên là giá thầu cao nhất
        Integer previousBidAmount = null; // Giá thầu trước đó
        for (Bid bid : bidPage) {
            Integer bidChange = 0; // Số tiền thay đổi

            // Tính toán số tiền thay đổi nếu có giá thầu trước đó
            if (previousBidAmount != null) {
                bidChange = bid.getBidAmount() - previousBidAmount; // Tính toán số tiền thay đổi
            }

            // Cập nhật giá thầu trước đó
            previousBidAmount = bid.getBidAmount();

            // Tạo đối tượng phản hồi
            BidResponse bidResponse = BidResponse.builder()
                    .bidAmount(bid.getBidAmount())
                    .bidTime(bid.getBidTime())
                    .winBid(bid.isWinBid())
                    .userId(bid.getUser().getId())
                    .auctionId(bid.getAuction().getAuctionId())
                    .email(bid.getUser().getUsername())
                    .username(bid.getUser().getFullName())
                    .build();

            bidResponses.add(bidResponse);
        }

        return ResponseEntity.ok(Map.of(
                "status", HttpStatus.OK,
                "message", "List of bids",
                "data", bidResponses,
                "totalElements", bidPage.getTotalElements(),
                "totalPages", bidPage.getTotalPages()
        ));
    }

    @Override
    public ResponseEntity<?> getInformationBid(Integer auctionId) {
        Auction auction = auctionRepository.findById(auctionId).orElse(null);
        if (auction == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .status(HttpStatus.NOT_FOUND)
                            .data(null)
                            .message("Auction not found")
                            .build()
            );
        }
        // Tìm danh sách bid theo thứ tự giảm dần của giá
        List<Bid> bids = bidRepository.findByAuction_AuctionIdOrderByBidAmountDesc(auction.getAuctionId());
        long bidCount = bidRepository.countByAuction_AuctionId(auctionId);
        double minimumBidPrice;
        if (bids.isEmpty()) {
            minimumBidPrice = auction.getStartPrice() + auction.getPriceStep();
        } else {
            minimumBidPrice = bids.get(0).getBidAmount() + auction.getPriceStep();
        }
        double minimumBidPrice1 = minimumBidPrice;
        double minimumBidPrice2 = minimumBidPrice1 + auction.getPriceStep();
        double minimumBidPrice3 = minimumBidPrice2 + auction.getPriceStep();
        BidInformation bidInformation = BidInformation.builder()
                .qualityBid((int) bidCount)
                .priceStep(auction.getPriceStep())
                .endDate(auction.getEndDate())
                .startDate(auction.getStartDate())
                .startTime(auction.getStartTime())
                .endTime(auction.getEndTime())
                .minimumBidPrice1(minimumBidPrice1)
                .minimumBidPrice2(minimumBidPrice2)
                .minimumBidPrice3(minimumBidPrice3)
                .build();

        return ResponseEntity.ok(
                ResponseObject.builder()
                        .status(HttpStatus.OK)
                        .data(bidInformation)
                        .message("Auction and bid information retrieved successfully")
                        .build()
        );
    }

    @Override
    public ResponseEntity<?> getBidDetail(Integer auctionId) {
        // Tìm kiếm thông tin của auction
        Auction auction = auctionRepository.findById(auctionId).orElse(null);
        if (auction == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                    .status(HttpStatus.NOT_FOUND)
                    .message("Auction not found")
                    .data(null)
                    .build());
        }

        // Lấy bid có giá cao nhất (nếu có)
        Page<Bid> highBid = bidRepository.findAllByAuction_AuctionIdOrderByBidAmountDesc(auctionId, PageRequest.of(0, 1));
        Bid highest = highBid.hasContent() ? highBid.getContent().get(0) : null;

        // Đếm số người tham gia
        int participantCount = bidRepository.countDistinctUsersByAuctionId(auctionId);

        // Đếm số bid đã được đặt
        long bidCount = bidRepository.countBidsByAuctionId(auctionId);

        // Kiểm tra nếu không có bid nào
        Double priceCurrent = (highest != null) ? highest.getBidAmount() : 0.0;

        // Tạo response
        BidDetailResponse bidDetailResponse = BidDetailResponse.builder()
                .numberOfBid((int) bidCount)
                .numberOfBider(participantCount)
                .itemDescription(auction.getItem().getItemDescription())
                .itemId(auction.getItem().getItemId())
                .itemName(auction.getItem().getItemName())
                .thumbnail(auction.getItem().getThumbnail())
                .startTime(auction.getStartTime())
                .endTime(auction.getEndTime())
                .priceCurrent(priceCurrent)
                .startDate(auction.getStartDate())
                .endDate(auction.getEndDate())
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder()
                .status(HttpStatus.OK)
                .message("Auction detail")
                .data(bidDetailResponse)
                .build());
    }

    @Override
    public ResponseEntity<?> getHighestBid(Integer auctionId) {
        var acution = bidRepository.findByAuction_AuctionId(auctionId);
        if (acution == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder().status(HttpStatus.NOT_FOUND)
                    .data(null)
                    .message("Auction not found")
                    .build());
        }
        Page<Bid> highBid = bidRepository.findAllByAuction_AuctionIdOrderByBidAmountDesc(auctionId, PageRequest.of(0, 1));
        Bid highest = highBid.hasContent() ? highBid.getContent().get(0) : null;

        return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder().status(HttpStatus.OK)
                .data(highest.getBidAmount())
                .message("Highest bid is " + highest.getBidAmount())
                .build());
    }


    public Bid findWinner(int auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("Auction not found"));

        List<Bid> bids = bidRepository.findByAuction_AuctionId(auctionId);

        if (bids.isEmpty()) {
            return null;
        }

        Bid winningBid = bids.stream()
                .filter(Bid::isWinBid)
                .max(Comparator.comparingInt(Bid::getBidAmount))
                .orElse(null);

        return winningBid;
    }

    public void sendBidUpdate(BidResponse bidResponse) {
        List<SseEmitter> deadEmitters = new ArrayList<>();
        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("bidUpdate")
                        .data(bidResponse)
                        .reconnectTime(3000));
            } catch (Exception e) {
                deadEmitters.add(emitter); // Lưu emitter lỗi để loại bỏ
            }
        });
        // Loại bỏ các emitter không còn hoạt động
        emitters.removeAll(deadEmitters);
    }
}
