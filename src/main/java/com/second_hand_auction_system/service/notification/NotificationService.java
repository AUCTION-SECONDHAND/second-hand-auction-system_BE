package com.second_hand_auction_system.service.notification;

import com.second_hand_auction_system.converters.notification.NotificationConverter;
import com.second_hand_auction_system.dtos.responses.ResponseObject;
import com.second_hand_auction_system.dtos.responses.notification.NotificationResponse;
import com.second_hand_auction_system.models.Auction;
import com.second_hand_auction_system.models.Bid;
import com.second_hand_auction_system.models.Notifications;
import com.second_hand_auction_system.models.User;
import com.second_hand_auction_system.repositories.*;
import com.second_hand_auction_system.service.jwt.IJwtService;
import com.second_hand_auction_system.utils.AuctionStatus;
import com.second_hand_auction_system.utils.NotificationStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService implements INotificationService {
    private final NotificationsRepository notificationRepository;
    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final AuctionRegistrationUserRepository auctionRegistrationUserRepository;
    private final IJwtService jwtService;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final NotificationConverter notificationConverter;

    public void sendNotification(String userId, Notifications notifications) {
        log.info("Sending ws notification to {}", userId, notifications);
        simpMessagingTemplate.convertAndSendToUser(
                userId,
                "/notifications",
                notifications
        );
    }

    @Override
    @Transactional
    public ResponseEntity<?> closeAuction(Integer auctionId) {
        String authHeader = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest().getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResponseObject.builder()
                    .status(HttpStatus.UNAUTHORIZED)
                    .message("UNAUTHORIZED")
                    .data(null)
                    .build()
            );
        }
        String token = authHeader.substring(7);
        String email = jwtService.extractUserEmail(token);
        User user = userRepository.findByEmailAndStatusIsTrue(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                    .status(HttpStatus.NOT_FOUND)
                    .message("User not found")
                    .data(null)
                    .build()
            );
        }
        // Kiểm tra nếu phiên đấu giá không tồn tại
        Auction auction = auctionRepository.findById(auctionId).orElse(null);
        if (auction == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .data(null)
                            .message("Auction not found")
                            .status(HttpStatus.NOT_FOUND)
                            .build());
        }
        if (auction.getStatus() != AuctionStatus.CLOSED) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .data(null)
                            .message("Auction is  open")
                            .status(HttpStatus.BAD_REQUEST)
                            .build());
        }

        // Lấy tất cả các bid của phiên đấu giá

        List<Bid> allBids = bidRepository.findByAuction_AuctionIdOrderByBidAmountDesc(auctionId);

        // Nếu không có bid nào, kết thúc phiên đấu giá và thông báo không có người thắng
        if (allBids.isEmpty()) {
            auction.setStatus(AuctionStatus.CLOSED);
            auctionRepository.save(auction);

            return ResponseEntity.status(HttpStatus.OK).body(
                    ResponseObject.builder()
                            .data(null)
                            .message("No bids, auction closed with no winner")
                            .status(HttpStatus.OK)
                            .build());
        }

        // Lấy bid cao nhất (người thắng cuộc)
        Bid winningBid = allBids.get(0);

        // Cập nhật trạng thái winBid cho các bid
        for (Bid bid : allBids) {
            bid.setWinBid(bid.equals(winningBid)); // Chỉ bid thắng cuộc có winBid = true
        }
        bidRepository.saveAll(allBids); // Lưu tất cả các bid

        // Cập nhật trạng thái phiên đấu giá thành 'CLOSED'
        auction.setStatus(AuctionStatus.CLOSED);
        auctionRepository.save(auction);
        List<User> userList = auctionRegistrationUserRepository.findAllByAuctionRegistration_Auction_AuctionId(auctionId);

        messagingTemplate.convertAndSend("/topic/auctionResult",
                "Auction " + auctionId + " closed. Winner: " + winningBid.getUser().getEmail());
        Notifications notifications;
        notifications = Notifications.builder()
                .message("THong bao ket qua")
                //.users(userList)
                .createBy(user.getFullName())
                .build();
        notificationRepository.save(notifications);
        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseObject.builder()
                        .data(winningBid)
                        .message("Auction closed, winner is " + winningBid.getUser().getEmail())
                        .status(HttpStatus.OK)
                        .build());
    }

    @Override
    public void createBidNotification(Integer userId, String title, String message) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Notifications notifications = Notifications.builder()
                .title(title)
                .message(message)
                .user(user)
                .status(true)
                .notificationStatus(NotificationStatus.SUCCESS)
                .build();
        notificationRepository.save(notifications);
    }

    @Override
    public List<NotificationResponse> getNotifications() throws Exception {
        String token = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
                .getRequest().getHeader("Authorization").substring(7);
        Integer userId = extractUserIdFromToken(token);
        if (userId == null) {
            throw new Exception("User not found");
        }
        List<Notifications> notifications = notificationRepository.findByUser_IdOrderByCreateAtDesc(userId);
        List<NotificationResponse> notificationResponses = notifications.stream()
                .map(notificationConverter::toNotificationResponse)
                .toList();
        return notificationResponses;
    }

    public Integer extractUserIdFromToken(String token) throws Exception {
        String userEmail = jwtService.extractUserEmail(token); // Extract email from token
        User user = userRepository.findByEmail(userEmail) // Find user by email
                .orElseThrow(() -> new Exception("User not found!!!"));
        return user.getId();
    }
}

