package com.second_hand_auction_system.service.transactionWallet;

import com.second_hand_auction_system.dtos.responses.ResponseObject;
import com.second_hand_auction_system.dtos.responses.transactionWallet.TransactionWalletResponse;
import com.second_hand_auction_system.dtos.responses.withdraw.APiResponse;
import com.second_hand_auction_system.models.Transaction;
import com.second_hand_auction_system.repositories.TransactionRepository;
import com.second_hand_auction_system.repositories.UserRepository;
import com.second_hand_auction_system.repositories.WalletRepository;
import com.second_hand_auction_system.service.jwt.IJwtService;
import com.second_hand_auction_system.utils.TransactionStatus;
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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionWalletService implements ITransactionWalletService {
    private final TransactionRepository transactionRepository;
    private final ModelMapper modelMapper;
    private final IJwtService jwtService;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    @Override
    public ResponseEntity<?> getAll(String keyword, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        List<Transaction> transactions;

//        if (keyword != null && !keyword.isEmpty()) {
////            transactionWallets = transactionWalletRepository.findByWalletCustomer_User_FullNameContainingAndCreateAtBetween(keyword, startDate, endDate, pageable);
//        } else if (startDate != null && endDate != null) {
//            transactionWallets = transactionWalletRepository.findTransactionWalletByCreateAt(startDate, endDate, pageable);
//        } else {
//            transactionWallets = transactionWalletRepository.findAll(pageable).getContent();
//        }
//
//        if (transactionWallets.isEmpty()) {
//            return ResponseEntity.noContent().build();
//        }

        return ResponseEntity.ok().body(ResponseObject.builder()
                .status(HttpStatus.OK)
                .message("List of transaction wallets")
                .data(null)
                .build());
    }

//    @Override
//    public ResponseEntity<?> getTransactionWallets(int size, int page) {
//        try {
//            Pageable pageable = PageRequest.of(page, size);
//            Page<TransactionWallet> transactionWalletsPage;
//            transactionWalletsPage = transactionWalletRepository.findAll(pageable);
//            if (transactionWalletsPage.isEmpty()) {
//                return ResponseEntity.ok(ResponseObject.builder()
//                        .data(null)
//                        .message("List of transaction wallets no content")
//                        .status(HttpStatus.NO_CONTENT)
//                        .build());
//            }
//
//            List<TransactionWalletResponse> listTransactionWalletResponse = transactionWalletsPage.getContent().stream()
//                    .map(wallet -> TransactionWalletResponse.builder()
//                            .transactionType(wallet.getTransactionType())
//                            .transactionId(wallet.getTransactionWalletId())
//                            .transactionStatus(wallet.getTransactionStatus())
//                            .transactionWalletCode(wallet.getTransactionWalletCode())
//                            .walletCustomerName(wallet.getWallet().getUser().getFullName())
//                            .amount((int) wallet.getAmount())
//                            .transactionDate(wallet.getCreateAt())
//                            .build())
//                    .collect(Collectors.toList());
//
//            return ResponseEntity.ok(ResponseObject.builder()
//                    .data(listTransactionWalletResponse)
//                    .message("List of transaction wallets")
//                    .status(HttpStatus.OK)
//                    .build());
//        } catch (Exception e) {
//            return ResponseEntity.ok(ResponseObject.builder()
//                    .data(null)
//                    .message(e.getMessage())
//                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .build());
//        }
//    }


    @Override
    public ResponseEntity<?> getTransactionById(int id) {
        Transaction transactionWallet = transactionRepository.findById(id).orElse(null);
        if (transactionWallet != null) {
            return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder()
                    .data(transactionWallet)
                    .message("Transaction wallet found")
                    .status(HttpStatus.OK)
                    .build());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                .data(null)
                .message("Transaction wallet found")
                .status(HttpStatus.OK)
                .build());
    }

    @Override
    public ResponseEntity<?> getTransactionWalletsBider(int size, int page) {
        Pageable pageable = PageRequest.of(page, size);
        String authHeader = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
                .getRequest().getHeader("Authorization");

        // Kiểm tra sự tồn tại của token và định dạng
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResponseObject.builder()
                    .status(HttpStatus.UNAUTHORIZED)
                    .data(null)
                    .message("Unauthorized: Token is missing or invalid")
                    .build());
        }

        // Trích xuất email từ token
        String token = authHeader.substring(7);
        String email = jwtService.extractUserEmail(token);

        var user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                    .status(HttpStatus.NOT_FOUND)
                    .data(null)
                    .message("User not found with email: " + email)
                    .build());
        }

        Page<Transaction> transactionWalletsPage = transactionRepository.findTransactionWalletByWallet_User_Id(user.getId(), pageable);

        List<TransactionWalletResponse> transactionWallets = transactionWalletsPage.getContent().stream()

                .map(transaction ->
                        TransactionWalletResponse.builder()
                        .transactionId(transaction.getTransactionWalletId())
                        .amount(transaction.getAmount())
                        .transactionWalletCode(transaction.getTransactionWalletCode())
                        .transactionType(transaction.getTransactionType())
                        .transactionStatus(transaction.getTransactionStatus())
                        .senderName(transaction.getSender())
                        .recipientName(transaction.getRecipient())
                        .transactionDate(transaction.getCreateAt())
                        .build())
                .collect(Collectors.toList());

        // Chuẩn bị phản hồi
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("data", transactionWallets); // Sử dụng danh sách đã ánh xạ
        responseData.put("totalPages", transactionWalletsPage.getTotalPages());
        responseData.put("totalElements", transactionWalletsPage.getTotalElements());

        return ResponseEntity.ok(ResponseObject.builder()
                .status(HttpStatus.OK)
                .data(responseData)
                .message("Transaction wallets retrieved successfully")
                .build());
    }



    @Override
    public ResponseEntity<?> updateTransaction(Integer transactionId, String vnpTransactionStatus) {
        String authHeader = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest().getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ResponseObject.builder()
                            .status(HttpStatus.UNAUTHORIZED)
                            .message("Unauthorized")
                            .data(null)
                            .build()
            );
        }
        String token = authHeader.substring(7);
        String email = jwtService.extractUserEmail(token);
        var user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                    .status(HttpStatus.NOT_FOUND)
                    .message("User not found")
                    .data(null)
                    .build());
        }
        var transactionType = transactionRepository.findById(transactionId).orElseThrow(null);
        var wallet = walletRepository.findByUserId(user.getId()).orElse(null);

        APiResponse response = new APiResponse();
        if (vnpTransactionStatus.equals("00")) {
            response.setCode("200");
            response.setMessage("Payment success");
            transactionType.setTransactionStatus(TransactionStatus.COMPLETED);
            transactionRepository.save(transactionType);
            if (wallet != null) {
                wallet.setBalance((wallet.getBalance() + transactionType.getAmount()));
                walletRepository.save(wallet);
            }

        } else {
            response.setCode("500");
            response.setMessage("Payment processing error");
            transactionType.setTransactionStatus(TransactionStatus.FAILED);
            transactionRepository.save(transactionType);
            if (wallet != null) {
                wallet.setBalance(wallet.getBalance());
                walletRepository.save(wallet);
            }
        }
        return ResponseEntity.ok(response);
    }


}
