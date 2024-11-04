package com.second_hand_auction_system.service.bid;

import com.second_hand_auction_system.converters.bid.BidConverter;
import com.second_hand_auction_system.dtos.request.bid.BidDto;
import com.second_hand_auction_system.dtos.request.bid.BidRequest;
import com.second_hand_auction_system.dtos.responses.ResponseObject;
import com.second_hand_auction_system.dtos.responses.bid.BidResponse;
import com.second_hand_auction_system.models.Auction;
import com.second_hand_auction_system.models.AuctionRegistration;
import com.second_hand_auction_system.models.Bid;
import com.second_hand_auction_system.models.User;
import com.second_hand_auction_system.repositories.AuctionRegistrationsRepository;
import com.second_hand_auction_system.repositories.AuctionRepository;
import com.second_hand_auction_system.repositories.BidRepository;
import com.second_hand_auction_system.repositories.UserRepository;
import com.second_hand_auction_system.service.email.EmailService;
import com.second_hand_auction_system.service.jwt.IJwtService;
import com.second_hand_auction_system.utils.AuctionStatus;
import com.second_hand_auction_system.utils.Registration;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.time.ZoneId;
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

    @Override
    @Transactional
    public ResponseEntity<?> createBid(BidRequest bidRequest) throws Exception {
        // Lấy token và kiểm tra người dùng
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

        // Kiểm tra phiên đấu giá
        Auction auction = auctionRepository.findById(bidRequest.getAuctionId()).orElse(null);
        if (auction == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .data(null)
                            .message("Auction not found")
                            .status(HttpStatus.NOT_FOUND)
                            .build());
        }
        var auctionRegister = auctionRegistrationsRepository.existsAuctionRegistrationByUserIdAndRegistration(requester.getId(), Registration.CONFIRMED);
        if (!auctionRegister) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .data(null)
                            .message("Bạn chưa đăng ký tham gia đấu giá")
                            .status(HttpStatus.BAD_REQUEST)
                            .build());
        }

        // Kiểm tra loại đấu giá
        if (!auction.getAuctionType().getAuctionTypeName().equals("Đấu giá kiểu Anh")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .data(null)
                            .message("Không thể đặt giá bid cho đấu giá ngược")
                            .status(HttpStatus.BAD_REQUEST)
                            .build());
        }

        // Kiểm tra trạng thái phiên đấu giá
        if (!auction.getStatus().equals(AuctionStatus.OPEN)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .data(null)
                            .message("Auction is not open for bidding")
                            .status(HttpStatus.BAD_REQUEST)
                            .build());
        }




        // Kiểm tra thời gian phiên đấu giá
        LocalDateTime auctionStartDateTime = auction.getStartDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .with(auction.getStartTime().toLocalTime());
        LocalDateTime auctionEndDateTime = auction.getEndDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .with(auction.getEndTime().toLocalTime());
        if (LocalDateTime.now().isBefore(auctionStartDateTime) || LocalDateTime.now().isAfter(auctionEndDateTime)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .data(null)
                            .message("Bidding is not allowed at this time")
                            .status(HttpStatus.BAD_REQUEST)
                            .build());
        }

        // Kiểm tra nếu người dùng đã có bid nào cho phiên đấu giá này
       Optional<Bid> existingUserBid = bidRepository.findByUserAndAuction(requester, auction);

        if(existingUserBid.isPresent()){
            Bid bid = existingUserBid.get();
            // Cập nhật bidAmount của bid hiện tại
            bid.setBidAmount(bidRequest.getBidAmount());
            bid.setBidTime(LocalDateTime.now()); // Cập nhật thời gian bid
            bidRepository.save(bid); // Lưu thay đổi
            messagingTemplate.convertAndSend("/topic/bids", bidRequest);

            return ResponseEntity.status(HttpStatus.OK).body(
                    ResponseObject.builder()
                            .data(null)
                            .message("Đã cập nhật giá đấu hiện tại")
                            .status(HttpStatus.OK)
                            .build());
        }


        // Kiểm tra số tiền bid hợp lệ
        Optional<Bid> existingBids = bidRepository.findByAuction_AuctionIdOrderByBidAmountDesc(auction.getAuctionId());
        Integer minimumRequiredBid;

        if (existingBids.isEmpty()) {


            // Không có bid nào trước đó, kiểm tra giá khởi điểm
            if (bidRequest.getBidAmount() < auction.getStartPrice()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        ResponseObject.builder()
                                .data(null)
                                .message("Bid amount must be at least the auction start price")
                                .status(HttpStatus.BAD_REQUEST)
                                .build());
            }



        } else {
            // Có bid trước đó
            Bid highestBid = existingBids.get(); // Lấy bid cao nhất từ Optional
            minimumRequiredBid = (int) (highestBid.getBidAmount() + auction.getPriceStep());
            if (bidRequest.getBidAmount() < minimumRequiredBid) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        ResponseObject.builder()
                                .data(null)
                                .message("Bid amount must be at least " + minimumRequiredBid)
                                .status(HttpStatus.BAD_REQUEST)
                                .build());
            }

            // Cập nhật winBid cho tất cả các bid trước đó thành false
            List<Bid> allBids = bidRepository.findByAuction_AuctionId(auction.getAuctionId());
            for (Bid bid : allBids) {
                bid.setWinBid(false);
                bidRepository.save(bid); // Lưu thay đổi cho mỗi bid
            }
        }

        // Lưu bid mới và thiết lập winBid cho bid mới
        Bid savedBid = bidRepository.save(
                Bid.builder()
                        .winBid(true) // Thiết lập winBid cho bid mới
                        .bidTime(LocalDateTime.now())
                        .auction(auction)
                        .user(requester)
                        .bidAmount(bidRequest.getBidAmount())
                        .build()
        );

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseObject.builder()
                        .data(null)
                        .message("Created new bid")
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
            emailService.sendBidNotification(email,requester.getFullName(),bidDto.getBidAmount(),existingBid.getBidAmount(),auction.getAuctionId());
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
       if(bid!= null){
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
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("Auction not found"));

        List<Bid> bids = bidRepository.findByAuction_AuctionId(auctionId);

        if (bids.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // No bids found
        }

        Bid winningBid = bids.stream()
                .filter(Bid::isWinBid)
                .max(Comparator.comparingInt(Bid::getBidAmount))
                .orElse(null);

        if (winningBid == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
//        Bid bidResponse = modelMapper.map(bids, BidResponse.class);
        BidResponse winningBidResponse = new BidResponse(); // Create a BidResponse object
//        winningBidResponse.setBidId(winningBid.getBidId());
        winningBidResponse.setBidAmount(winningBid.getBidAmount());
//        winningBidResponse.setBidTime(winningBid.getBidTime());
//        winningBidResponse.setBidStatus(winningBid.getBidStatus());
//        winningBidResponse.setWinBid(winningBid.isWinBid());
        winningBidResponse.setUserId(winningBid.getUser().getId());
        winningBidResponse.setAuctionId(auctionId);
//
        return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder()
                .status(HttpStatus.OK)
                .message("Find winner")
                .data(winningBidResponse)
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
                    .username(bid.getUser().getUsername())
                    .bidChange(bidChange) // Thêm số tiền thay đổi vào phản hồi
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


}
