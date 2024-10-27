package com.second_hand_auction_system.service.auctionRegistrations;

import com.second_hand_auction_system.converters.auctionRegistrations.AuctionRegistrationsConverter;
import com.second_hand_auction_system.dtos.request.auctionRegistrations.AuctionRegistrationsDto;
import com.second_hand_auction_system.dtos.responses.ResponseObject;
import com.second_hand_auction_system.dtos.responses.auctionRegistrations.AuctionRegistrationsResponse;
import com.second_hand_auction_system.dtos.responses.auctionRegistrations.CheckStatusAuctionRegisterResponse;
import com.second_hand_auction_system.models.*;
import com.second_hand_auction_system.repositories.*;
import com.second_hand_auction_system.service.jwt.IJwtService;
import com.second_hand_auction_system.service.walletCustomer.WalletCustomerService;
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
    private final WalletCustomerRepository walletCustomerRepository;
    private final TransactionWalletRepository transactionWalletRepository;
    private final IJwtService jwtService;
    private final AuctionRegistrationsConverter auctionRegistrationsConverter;
    private final WalletSystemRepository walletSystemRepository;

    @Override
    @Transactional
    public ResponseEntity<?> addAuctionRegistration(AuctionRegistrationsDto auctionRegistrationsDto) throws Exception {
        String authHeader = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest().getHeader("Authorization");

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
        WalletCustomer walletCustomer = walletCustomerRepository.findByUserId(requester.getId()).orElse(null);
        if (walletCustomer == null || walletCustomer.getBalance() < 0) {
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
            // Tính số tiền cọc
            double depositAmount = 0.1 * auctionExist.getStartPrice();

            // Kiểm tra số dư ví có đủ cho tiền cọc không
            if (walletCustomer.getBalance() < depositAmount) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseObject.builder()
                        .status(HttpStatus.BAD_REQUEST)
                        .data(null)
                        .message("You do not have enough money in your wallet for the deposit amount")
                        .build());
            }

            // Kiểm tra đăng ký hiện có
            // Tìm kiếm bản ghi AuctionRegistration theo auction ID
            AuctionRegistration auctionRegistration = auctionRegistrationsRepository.findByAuction_AuctionId(auctionRegistrationsDto.getAuction()).orElse(null);

            if (auctionRegistration == null) {
                // Chèn mới
                Set<User> users = new HashSet<>(); // Tạo một Set mới để chứa User
                users.add(requester); // Thêm requester vào Set

                AuctionRegistration createAuctionRegistration = AuctionRegistration.builder()
                        .registration(Registration.CONFIRMED)
                        .auction(auctionExist)
                        .users(users) // Sử dụng Set đã tạo
                        .depositeAmount(depositAmount)
                        .build();
                auctionRegistrationsRepository.save(createAuctionRegistration);
            } else {
                // Cập nhật bản ghi đã tồn tại
                auctionRegistration.setRegistration(Registration.CONFIRMED);
                auctionRegistration.getUsers().add(requester); // Thêm requester vào Set hiện có
                auctionRegistration.setDepositeAmount(auctionRegistration.getDepositeAmount() + depositAmount);
                auctionRegistrationsRepository.save(auctionRegistration);
            }

            // Trừ tiền cọc từ ví của người dùng
            walletCustomer.setBalance(walletCustomer.getBalance() - depositAmount);
            walletCustomerRepository.save(walletCustomer);

            // Cập nhật số dư ví của admin
            WalletSystem walletSystem = walletSystemRepository.findFirstByOrderByWalletAdminIdAsc().orElse(null);
            assert walletSystem != null;
            walletSystem.setBalance(walletSystem.getBalance() + depositAmount);

            // Lưu giao dịch
            double commissionRate = 0.05; // 5%
            long commissionAmount = (long) (depositAmount * commissionRate);
            TransactionWallet transactionWallet = TransactionWallet.builder()
                    .transactionType(TransactionType.DEPOSIT_AUCTION)
                    .amount((long) depositAmount)
                    .transactionStatus(TransactionStatus.COMPLETED)
                    .walletSystem(walletSystem)
                    .walletCustomer(walletCustomer)
                    .commissionAmount((int) commissionAmount)
                    .commissionRate(commissionRate)
                    .transactionWalletCode(generate())
                    .build();
            transactionWalletRepository.save(transactionWallet);

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


    private static long generate() {
        SecureRandom secureRandom = new SecureRandom();
        long randomNumber = 100000 + secureRandom.nextInt(900000); // nextInt(900000) tạo số từ 0 đến 899999
        return randomNumber;
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

//    @Override
//    public Page<AuctionRegistrationsResponse> findAllAuctionRegistrationsByUserId(PageRequest pageRequest) throws Exception {
//        String token = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
//                .getRequest().getHeader("Authorization").substring(7);
//        Integer userId = extractUserIdFromToken(token);
//        Page<AuctionRegistration> auctionRegistrations = auctionRegistrationsRepository.findByUserId(userId, pageRequest);
//        return auctionRegistrations.map(auctionRegistrationsConverter::toAuctionRegistrationsResponse);
//    }

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
                .findByUsersIdAndAuction_AuctionId(user,auction.getAuctionId());
        return CheckStatusAuctionRegisterResponse.builder()
                .auctionId(auction.getAuctionId())
                .userId(user)
                .registration(checkStatusAuctionRegisterResponse.getRegistration())
                .build();
    }


    public Integer extractUserIdFromToken(String token) throws Exception {
        String userEmail = jwtService.extractUserEmail(token); // Extract email from token
        User user = userRepository.findByEmail(userEmail) // Find user by email
                .orElseThrow(() -> new Exception("User not found!!!"));
        return user.getId();
    }
}
