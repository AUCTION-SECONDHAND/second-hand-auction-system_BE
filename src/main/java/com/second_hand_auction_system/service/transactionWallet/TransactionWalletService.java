package com.second_hand_auction_system.service.transactionWallet;

import com.second_hand_auction_system.dtos.responses.ResponseObject;
import com.second_hand_auction_system.dtos.responses.transactionWallet.ListTransactionWalletResponse;
import com.second_hand_auction_system.dtos.responses.transactionWallet.TransactionWalletResponse;
import com.second_hand_auction_system.models.TransactionWallet;
import com.second_hand_auction_system.repositories.TransactionWalletRepository;
import com.second_hand_auction_system.repositories.UserRepository;
import com.second_hand_auction_system.service.jwt.IJwtService;
import lombok.RequiredArgsConstructor;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionWalletService implements ITransactionWalletService {
    private final TransactionWalletRepository transactionWalletRepository;
    private final ModelMapper modelMapper;
    private final IJwtService jwtService;
    private final UserRepository userRepository;

    @Override
    public ResponseEntity<?> getAll(String keyword, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        List<TransactionWallet> transactionWallets;

        if (keyword != null && !keyword.isEmpty()) {
            transactionWallets = transactionWalletRepository.findByWalletCustomer_User_FullNameContainingAndCreateAtBetween(keyword, startDate, endDate, pageable);
        } else if (startDate != null && endDate != null) {
            transactionWallets = transactionWalletRepository.findTransactionWalletByCreateAt(startDate, endDate, pageable);
        } else {
            transactionWallets = transactionWalletRepository.findAll(pageable).getContent();
        }

        if (transactionWallets.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok().body(ResponseObject.builder()
                .status(HttpStatus.OK)
                .message("List of transaction wallets")
                .data(transactionWallets)
                .build());
    }

    @Override
    public ResponseEntity<?> getTransactionWallets(int size, int page) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<TransactionWallet> transactionWalletsPage;
            transactionWalletsPage = transactionWalletRepository.findAll(pageable);
            if (transactionWalletsPage.isEmpty()) {
                return ResponseEntity.ok(ResponseObject.builder()
                        .data(null)
                        .message("List of transaction wallets no content")
                        .status(HttpStatus.NO_CONTENT)
                        .build());
            }

            List<TransactionWalletResponse> listTransactionWalletResponse = transactionWalletsPage.getContent().stream()
                    .map(wallet -> TransactionWalletResponse.builder()
                            .transactionType(wallet.getTransactionType())
                            .transactionId(wallet.getTransactionWalletId())
                            .transactionStatus(wallet.getTransactionStatus())
                            .transactionWalletCode(wallet.getTransactionWalletCode())
                            .walletCustomerName(wallet.getWalletCustomer().getUser().getFullName())
                            .amount((int) wallet.getAmount())
                            .transactionDate(wallet.getCreateAt())
                            .build())
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ResponseObject.builder()
                    .data(listTransactionWalletResponse)
                    .message("List of transaction wallets")
                    .status(HttpStatus.OK)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(ResponseObject.builder()
                    .data(null)
                    .message(e.getMessage())
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build());
        }
    }


    @Override
    public ResponseEntity<?> getTransactionById(int id) {
        TransactionWallet transactionWallet = transactionWalletRepository.findById(id).orElse(null);
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
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResponseObject.builder()
                    .status(HttpStatus.UNAUTHORIZED)
                    .data(null)
                    .message("Unauthorized")
                    .build());
        }
        String token = authHeader.substring(7);
        String email = jwtService.extractUserEmail(token);
        var user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                    .status(HttpStatus.NOT_FOUND)
                    .data(null)
                    .message("User not found")
                    .build());
        }

        Page<TransactionWallet> transactionWalletsPage = transactionWalletRepository
                .findTransactionWalletByWalletCustomer_User_Id(user.getId(), pageable);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("data", transactionWalletsPage.getContent());

        responseData.put("totalPages", transactionWalletsPage.getTotalPages());
        responseData.put("totalElements", transactionWalletsPage.getTotalElements());

        return ResponseEntity.ok(ResponseObject.builder()
                .status(HttpStatus.OK)
                .data(responseData)
                .message("Transaction wallets retrieved successfully")
                .build());
    }





}
