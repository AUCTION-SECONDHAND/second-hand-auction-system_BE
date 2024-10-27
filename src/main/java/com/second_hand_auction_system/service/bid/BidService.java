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
import com.second_hand_auction_system.service.jwt.IJwtService;
import com.second_hand_auction_system.utils.AuctionStatus;
import com.second_hand_auction_system.utils.Registration;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BidService implements IBidService {
    private final BidRepository bidRepository;
    private final IJwtService jwtService;
    private final UserRepository userRepository;
    private final AuctionRegistrationsRepository auctionRegistrationsRepository;
    private final AuctionRepository auctionRepository;
    private final ModelMapper modelMapper;

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
        if(!auctionRegister) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .data(null)
                            .message("Ban chua dang ki tham gia dau gia")
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

        // Kiểm tra số tiền bid hợp lệ
        Optional<Bid> existingBids = bidRepository.findByAuction_AuctionIdOrderByBidAmountDesc(auction.getAuctionId());
        if (existingBids.isEmpty()) {
            if (bidRequest.getBidAmount() < auction.getStartPrice()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        ResponseObject.builder()
                                .data(null)
                                .message("Bid amount must be at least the auction start price")
                                .status(HttpStatus.BAD_REQUEST)
                                .build());
            }
        } else {
            Bid highestBid = existingBids.get();
            int minimumRequiredBid = (int) (highestBid.getBidAmount() + auction.getPriceStep());
            if (bidRequest.getBidAmount() < minimumRequiredBid) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        ResponseObject.builder()
                                .data(null)
                                .message("Bid amount must be at least " + minimumRequiredBid)
                                .status(HttpStatus.BAD_REQUEST)
                                .build());
            }
        }

        // Lưu bid và trả về phản hồi thành công
        Bid savedBid = bidRepository.save(
                Bid.builder()
                        .winBid(true)
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
    public BidResponse updateBid(Integer bidId, BidDto bidDto) throws Exception {
        // Find existing bid
        Bid existingBid = bidRepository.findById(bidId)
                .orElseThrow(() -> new Exception("Bid not found"));

        // Find User and Auction entities
        User user = userRepository.findById(bidDto.getUserId())
                .orElseThrow(() -> new Exception("User not found"));
        Auction auction = auctionRepository.findById(bidDto.getAuctionId())
                .orElseThrow(() -> new Exception("Auction not found"));

        // Update bid details
        existingBid.setBidAmount(bidDto.getBidAmount());
//        existingBid.setBidTime(bidDto.getBidTime());
//        existingBid.setBidStatus(bidDto.getBidStatus());
        existingBid.setWinBid(bidDto.isWinBid());
        existingBid.setUser(user);
        existingBid.setAuction(auction);

        // Save updated bid
        Bid updatedBid = bidRepository.save(existingBid);

        // Convert entity to response
        return BidConverter.convertToResponse(updatedBid);
    }

    @Override
    public void deleteBid(Integer bidId) throws Exception {
        // Find bid
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new Exception("Bid not found"));

        // Delete bid
        bidRepository.delete(bid);
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
        winningBidResponse.setBidId(winningBid.getBidId());
        winningBidResponse.setBidAmount(winningBid.getBidAmount());
//        winningBidResponse.setBidTime(winningBid.getBidTime());
//        winningBidResponse.setBidStatus(winningBid.getBidStatus());
        winningBidResponse.setWinBid(winningBid.isWinBid());
        winningBidResponse.setUserId(winningBid.getUser().getId());
        winningBidResponse.setAuctionId(auctionId);
//
        return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder()
                .status(HttpStatus.OK)
                .message("Find winner")
                .data(winningBidResponse)
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


}
