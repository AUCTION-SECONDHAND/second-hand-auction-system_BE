package com.second_hand_auction_system.service.auctionRegistrations;

import com.second_hand_auction_system.converters.auctionRegistrations.AuctionRegistrationsConverter;
import com.second_hand_auction_system.dtos.request.auctionRegistrations.AuctionRegistrationsDto;
import com.second_hand_auction_system.dtos.responses.ResponseObject;
import com.second_hand_auction_system.dtos.responses.auctionRegistrations.AuctionRegistrationsResponse;
import com.second_hand_auction_system.dtos.responses.auctionRegistrations.CheckStatusAuctionRegisterResponse;
import com.second_hand_auction_system.models.*;
import com.second_hand_auction_system.repositories.*;
import com.second_hand_auction_system.service.jwt.IJwtService;
import com.second_hand_auction_system.utils.AuctionStatus;
import com.second_hand_auction_system.utils.Registration;
import com.second_hand_auction_system.utils.TransactionStatus;
import com.second_hand_auction_system.utils.TransactionType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuctionRegistrationsService implements IAuctionRegistrationsService {
    private final AuctionRegistrationsRepository auctionRegistrationsRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final AuctionRepository auctionRepository;
    private final TransactionRepository transactionRepository;
    private final IJwtService jwtService;
    private final AuctionRegistrationsConverter auctionRegistrationsConverter;
    private final WalletRepository walletRepository;

    @Override
    @Transactional
    public ResponseEntity<?> addAuctionRegistration(AuctionRegistrationsDto auctionRegistrationsDto) throws Exception {
        String authHeader = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
                .getRequest().getHeader("Authorization");

        // Kiểm tra Authorization header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResponseObject.builder()
                    .status(HttpStatus.UNAUTHORIZED)
                    .data(null)
                    .message("Unauthorized")
                    .build());
        }

        String token = authHeader.substring(7);
        String userEmail = jwtService.extractUserEmail(token);

        // Kiểm tra người dùng
        User requester = userRepository.findByEmailAndStatusIsTrue(userEmail).orElse(null);
        if (requester == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                    .status(HttpStatus.NOT_FOUND)
                    .data(null)
                    .message("User not found")
                    .build());
        }

        // Kiểm tra ví của người dùng
        Wallet userWallet = walletRepository.findByUserId(requester.getId()).orElse(null);
        if (userWallet == null || userWallet.getBalance() <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseObject.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .data(null)
                    .message("Wallet not found or balance is insufficient")
                    .build());
        }

        // Kiểm tra phiên đấu giá
        Auction auctionExist = auctionRepository.findById(auctionRegistrationsDto.getAuction()).orElse(null);
        if (auctionExist == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                    .status(HttpStatus.NOT_FOUND)
                    .data(null)
                    .message("Auction not found")
                    .build());
        }

        if (auctionExist.getStatus().equals(AuctionStatus.OPEN)) {
            // Kiểm tra xem người dùng đã đăng ký phiên này chưa
            Optional<AuctionRegistration> existingRegistration = auctionRegistrationsRepository
                    .findByAuction_AuctionIdAndUsers_Id(auctionRegistrationsDto.getAuction(), requester.getId());

            if (existingRegistration.isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(ResponseObject.builder()
                        .status(HttpStatus.CONFLICT)
                        .data(null)
                        .message("You have already registered for this auction")
                        .build());
            }

            // Tính số tiền cọc 10% giá khởi điểm
            double depositAmount = 0.1 * auctionExist.getStartPrice();

            // Kiểm tra số dư ví có đủ cho tiền cọc không
            if (userWallet.getBalance() < depositAmount) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseObject.builder()
                        .status(HttpStatus.BAD_REQUEST)
                        .data(null)
                        .message("You do not have enough money in your wallet for the deposit amount")
                        .build());
            }

            // Thêm đăng ký mới
            List<User> userAuction = new ArrayList<>();
            userAuction.add(requester);

            AuctionRegistration newRegistration = AuctionRegistration.builder()
                    .registration(Registration.CONFIRMED)
                    .auction(auctionExist)
                    .users(userAuction)
                    .depositeAmount(depositAmount)
                    .build();
            auctionRegistrationsRepository.save(newRegistration);

            // Trừ tiền từ ví của người dùng và cộng vào ví đấu giá
            userWallet.setBalance(userWallet.getBalance() - depositAmount);
            walletRepository.save(userWallet);

            Wallet auctionWallet = walletRepository.findWalletByAuctionId(auctionExist.getAuctionId()).orElse(null);
            if (auctionWallet != null) {
                auctionWallet.setBalance(auctionWallet.getBalance() + depositAmount);
                walletRepository.save(auctionWallet);
            }

            // Lưu giao dịch với mức hoa hồng 5%
            double commissionRate = 0.05;
            double commissionAmount = depositAmount * commissionRate;
            Transaction transactionWallet = Transaction.builder()
                    .transactionType(TransactionType.DEPOSIT_AUCTION)
                    .amount((long) depositAmount)
                    .transactionStatus(TransactionStatus.COMPLETED)
                    .recipient("SYSTEM")
                    .sender(requester.getFullName())
                    .commissionAmount((int) commissionAmount)
                    .commissionRate(commissionRate)
                    .transactionWalletCode(generateTransactionCode())
                    .build();
            transactionRepository.save(transactionWallet);

            return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder()
                    .status(HttpStatus.OK)
                    .data(null)
                    .message("Registered auction successfully")
                    .build());
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseObject.builder()
                .status(HttpStatus.BAD_REQUEST)
                .data(null)
                .message("Auction is closed")
                .build());
    }

    private static long generateTransactionCode() {
        SecureRandom secureRandom = new SecureRandom();
        return 100000 + secureRandom.nextInt(900000);
    }



    @Override
    public void updateAuctionRegistration(int arId, AuctionRegistrationsDto auctionRegistrationsDto) throws Exception {

    }

    @Override
    public void removeAuctionRegistration(int arId) throws Exception {

    }

    @Override
    public Page<AuctionRegistrationsResponse> findAllAuctionRegistrations(PageRequest pageRequest) throws Exception {
        Page<AuctionRegistration> auctionRegistrations = auctionRegistrationsRepository.findAll(pageRequest);
        return auctionRegistrations.map(auctionRegistrationsConverter::toAuctionRegistrationsResponse);
    }

    @Override
    public Page<AuctionRegistrationsResponse> findAllAuctionRegistrationsByUserId(PageRequest pageRequest) throws Exception {
        String token = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
                .getRequest().getHeader("Authorization").substring(7);
        Integer userId = extractUserIdFromToken(token);
        Page<AuctionRegistration> auctionRegistrations = auctionRegistrationsRepository.findAuctionRegistrationsByUserId(userId, pageRequest);
        return auctionRegistrations.map(auctionRegistrationsConverter::toAuctionRegistrationsResponse);
    }

    @Override
    public AuctionRegistrationsResponse findAuctionRegistrationById(int arId) throws Exception {
        AuctionRegistration auctionRegistration = auctionRegistrationsRepository.findById(arId)
                .orElseThrow(() -> new RuntimeException("Auction not found"));
        AuctionRegistrationsResponse auctionRegistrationsResponse = auctionRegistrationsConverter.toAuctionRegistrationsResponse(auctionRegistration);
        return auctionRegistrationsResponse;
    }

    @Override
    public List<CheckStatusAuctionRegisterResponse> getRegistrationsByUserId() throws Exception {
        String token = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
                .getRequest().getHeader("Authorization").substring(7);
        Integer user = extractUserIdFromToken(token);
        List<AuctionRegistration> auctionRegistrations = auctionRegistrationsRepository.findByUsersId(user);

        return auctionRegistrations.stream()
                .map(registration -> {
                    // Get the first userId (or handle differently as needed)
                    Integer userId = registration.getUsers().stream()
                            .findFirst()
                            .map(User::getId) // Assuming User class has getId() method
                            .orElse(null); // or handle the case where no user exists

                    return new CheckStatusAuctionRegisterResponse(
                            userId, // Use the userId extracted from the stream
                            registration.getAuction().getAuctionId(),
                            registration.getRegistration()
                    );
                })
                .collect(Collectors.toList());
    }


    @Override
    public CheckStatusAuctionRegisterResponse getRegistrationsByUserIdAnhAuctionId(Integer auctionId) throws Exception {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Auction not found"));
        String token = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
                .getRequest().getHeader("Authorization").substring(7);
        Integer user = extractUserIdFromToken(token);
//        CheckStatusAuctionRegisterResponse checkStatusAuctionRegisterResponse = auctionRegistrationsRepository
//                .findByUserIdAndAuction_AuctionId(user,auction.getAuctionId());
        AuctionRegistration checkStatusAuctionRegisterResponse = auctionRegistrationsRepository
                .findByUsersIdAndAuction_AuctionId(user, auction.getAuctionId());
        return CheckStatusAuctionRegisterResponse.builder()
                .auctionId(auction.getAuctionId())
                .userId(user)
                .registration(checkStatusAuctionRegisterResponse.getRegistration())
                .build();
    }

    @Override
    public Map<String, Object> checkUserInAuction(Integer auctionId) throws Exception {
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

        int userId = requester.getId();
        System.out.println("Checking userId: " + userId + " for auctionId: " + auctionId);

        // Lấy auctionRegistrationId từ auctionId
        Integer auctionRegistrationId = auctionRegistrationsRepository.findAuctionRegistrationIdByAuctionId(auctionId);
        if (auctionRegistrationId == null) {
            throw new Exception("Auction registration not found for the given auctionId");
        }

        boolean exists = auctionRegistrationsRepository.existsByUserIdAndAuctionRegistrationId(userId, auctionRegistrationId);

        // Tạo Map để trả về
        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("exists", exists);

        return response;
    }


    public Integer extractUserIdFromToken(String token) throws Exception {
        String userEmail = jwtService.extractUserEmail(token); // Extract email from token
        User user = userRepository.findByEmail(userEmail) // Find user by email
                .orElseThrow(() -> new Exception("User not found!!!"));
        return user.getId();
    }
}
