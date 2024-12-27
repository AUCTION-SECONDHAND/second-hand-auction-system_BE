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
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
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
    private final AuctionRegistrationUserRepository registrationUserRepository;

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
                    .message("Số dư ví của bạn không đủ")
                    .build());
        }

        // Kiểm tra phiên đấu giá
        Auction auctionExist = auctionRepository.findById(auctionRegistrationsDto.getAuction()).orElse(null);
        if (auctionExist == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                    .status(HttpStatus.NOT_FOUND)
                    .data(null)
                    .message("Phiên đấu giá không tìm thấy")
                    .build());
        }

        if (auctionExist.getStatus().equals(AuctionStatus.PENDING)) {
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

            // Tính số tiền cọc bằng giá tiền mong muốn x phần trăm cọc
            double depositAmount = (auctionExist.getPercentDeposit() * auctionExist.getBuyNowPrice()/100);

            // Kiểm tra số dư ví có đủ cho tiền cọc không
            if (userWallet.getBalance() < depositAmount) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseObject.builder()
                        .status(HttpStatus.BAD_REQUEST)
                        .data(null)
                        .message("Số dư ví hiện tại không đủ để thực hiện giao dịch. Vui lòng nạp thêm tiền vào ví.")
                        .build());
            }

            // Thêm đăng ký mới
            List<User> userAuction = new ArrayList<>();
            userAuction.add(requester);

            AuctionRegistration newRegistration = AuctionRegistration.builder()
                    .registration(true)
                    .auction(auctionExist)
                    .users(userAuction)
                    .depositeAmount(depositAmount)
                    .build();
            auctionRegistrationsRepository.save(newRegistration);
            log.info("Vi user: " + userWallet.getBalance());
            // Trừ tiền từ ví của người dùng và cộng vào ví đấu giá
            userWallet.setBalance(userWallet.getBalance() - depositAmount);
            walletRepository.save(userWallet);
            log.info("Vi user sau khi coc: " + userWallet.getBalance());
            Wallet viCoc = walletRepository.findWalletByAuctionId(auctionExist.getWallet().getWalletId()).orElse(null);
            if (viCoc != null) {
                log.info("Vi coc ban dau: " + viCoc.getBalance());
                viCoc.setBalance(viCoc.getBalance() + depositAmount);
                walletRepository.save(viCoc);
                log.info("Vi coc: " + viCoc.getBalance());
            }

            AuctionRegistrationUser auctionRegistrationUser = registrationUserRepository.findByAuctionRegistration_AuctionRegistrationId(newRegistration.getAuctionRegistrationId()).orElse(null);
            if (auctionRegistrationUser != null) {
                auctionRegistrationUser.setStatusRegistration(true);
                auctionRegistrationUser.setCreateAt(LocalDateTime.now());
                auctionRegistrationUser.setUpdateAt(LocalDateTime.now());
                registrationUserRepository.save(auctionRegistrationUser);
            }

//            Transaction transactionWallet = Transaction.builder()
//                    .transactionType(TransactionType.DEPOSIT_AUCTION)
//                    .amount(+(long) depositAmount)
//                    .transactionStatus(TransactionStatus.COMPLETED)
//                    .recipient("SYSTEM")
//                    .sender(requester.getFullName())
//                    .commissionAmount(0)
//                    .commissionRate(0)
//                    .transactionWalletCode(generateTransactionCode())
//                    .build();
//            transactionRepository.save(transactionWallet);
            //tracsaction cuar thang nap
            Transaction transactionUser = Transaction.builder()
                    .transactionType(TransactionType.DEPOSIT_AUCTION)
                    .amount(-(long) depositAmount)
                    .transactionStatus(TransactionStatus.COMPLETED)
                    .recipient("Hệ thống đấu giá phiên" + auctionExist.getAuctionId())
                    .description("Nạp tiền cọc tham gia đấu giá ")
                    .sender(requester.getFullName())
                    .commissionAmount(0)
                    .commissionRate(0)
                    .wallet(userWallet)
                    .transactionWalletCode(generateTransactionCode())
                    .build();
            transactionRepository.save(transactionUser);
            return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder()
                    .status(HttpStatus.OK)
                    .data(null)
                    .message("Registered auction successfully")
                    .build());
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseObject.builder()
                .status(HttpStatus.BAD_REQUEST)
                .data(null)
                .message("Phien dau gia da ket thuc")
                .build());
    }

    private static long generateTransactionCode() {
        SecureRandom secureRandom = new SecureRandom();
        return 10000000L + secureRandom.nextInt(90000000); // Random từ 10000000 đến 99999999
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
//                            registration
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
        AuctionRegistrationUser auctionUserDeposite = registrationUserRepository
                .findByAuctionRegistration_Auction_AuctionIdAndUser_Id(auctionId, user)
                .orElseThrow(() -> new RuntimeException("AuctionUserDeposite not found"));
//        CheckStatusAuctionRegisterResponse checkStatusAuctionRegisterResponse = auctionRegistrationsRepository
//                .findByUserIdAndAuction_AuctionId(user,auction.getAuctionId());
        AuctionRegistration checkStatusAuctionRegisterResponse = auctionRegistrationsRepository
                .findByUsersIdAndAuction_AuctionId(user, auction.getAuctionId());
        return CheckStatusAuctionRegisterResponse.builder()
                .auctionId(auction.getAuctionId())
                .userId(user)
//                .registration(checkStatusAuctionRegisterResponse.getRegistration())
                .statusRegistration(auctionUserDeposite.getStatusRegistration())
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

    @Override
    public Page<AuctionRegistrationsResponse> findUsersRegisteredByAuctionId(Integer auctionId, Pageable pageable) throws Exception {
        // Lấy các auction registrations theo auctionId và sắp xếp theo ngày tạo
        Page<AuctionRegistration> auctionRegistrationsPage = auctionRegistrationsRepository.findByAuction_AuctionIdOrderByCreateAtDesc(auctionId, pageable);

        // Chuyển đổi Page<AuctionRegistration> thành Page<AuctionRegistrationsResponse>
        return auctionRegistrationsPage.map(auctionRegistration -> auctionRegistrationsConverter.toDetailedResponse(auctionRegistration));
    }






    public Integer extractUserIdFromToken(String token) throws Exception {
        String userEmail = jwtService.extractUserEmail(token); // Extract email from token
        User user = userRepository.findByEmail(userEmail) // Find user by email
                .orElseThrow(() -> new Exception("User not found!!!"));
        return user.getId();
    }
}
