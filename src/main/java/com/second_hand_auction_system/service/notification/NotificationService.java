package com.second_hand_auction_system.service.notification;

import com.second_hand_auction_system.dtos.responses.ResponseObject;
import com.second_hand_auction_system.models.Auction;
import com.second_hand_auction_system.models.Bid;
import com.second_hand_auction_system.models.User;
import com.second_hand_auction_system.repositories.AuctionRegistrationUserRepository;
import com.second_hand_auction_system.repositories.AuctionRepository;
import com.second_hand_auction_system.repositories.BidRepository;
import com.second_hand_auction_system.utils.AuctionStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
public class NotificationService implements INotificationService {
//    private final NotificationRepository notificationRepository;
    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final AuctionRegistrationUserRepository auctionRegistrationUserRepository;
    @Override
    @Transactional
    public ResponseEntity<?> closeAuction( Integer auctionId) {
        String authHeader = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest().getHeader("Authorization");
        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
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
//        Notifications notification = Notifications.builder()
//                .message("THong bao ket qua")
//                .users(userList)
//                .createBy()
        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseObject.builder()
                        .data(winningBid)
                        .message("Auction closed, winner is " + winningBid.getUser().getEmail())
                        .status(HttpStatus.OK)
                        .build());
    }
}

