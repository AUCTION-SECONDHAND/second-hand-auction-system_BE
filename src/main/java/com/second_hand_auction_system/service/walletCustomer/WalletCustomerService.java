package com.second_hand_auction_system.service.walletCustomer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.second_hand_auction_system.dtos.request.walletCustomer.Deposit;
import com.second_hand_auction_system.dtos.request.walletCustomer.PaymentRequest;
import com.second_hand_auction_system.dtos.responses.ResponseObject;
import com.second_hand_auction_system.models.TransactionSystem;
import com.second_hand_auction_system.models.TransactionWallet;
import com.second_hand_auction_system.models.User;
import com.second_hand_auction_system.models.WalletCustomer;
import com.second_hand_auction_system.repositories.TransactionSystemRepository;
import com.second_hand_auction_system.repositories.TransactionWalletRepository;
import com.second_hand_auction_system.repositories.UserRepository;
import com.second_hand_auction_system.repositories.WalletCustomerRepository;
import com.second_hand_auction_system.service.email.EmailService;
import com.second_hand_auction_system.service.jwt.IJwtService;
import com.second_hand_auction_system.utils.Role;
import com.second_hand_auction_system.utils.StatusWallet;
import com.second_hand_auction_system.utils.TransactionStatus;
import com.second_hand_auction_system.utils.TransactionType;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import vn.payos.PayOS;
import vn.payos.type.*;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WalletCustomerService implements IWalletCustomerService {
    private final WalletCustomerRepository walletCustomerRepository;
    private final PayOS payOS;
    private final IJwtService jwtService;
    private final UserRepository userRepository;
    private final TransactionWalletRepository transactionWalletRepository;
    private final TransactionSystemRepository transactionSystemRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> depositWallet(Deposit deposit) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();

        try {
            String authHeader = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
                    .getRequest().getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ResponseObject.builder()
                                .status(HttpStatus.UNAUTHORIZED)
                                .message("Missing or invalid Authorization header")
                                .build());
            }

            String token = authHeader.substring(7);
            String userEmail = jwtService.extractUserEmail(token);
            User requester = userRepository.findByEmailAndStatusIsTrue(userEmail).orElse(null);

            if (requester == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ResponseObject.builder()
                                .status(HttpStatus.UNAUTHORIZED)
                                .message("Unauthorized request - User not found")
                                .build());
            }

            if (requester.getRole() == Role.BUYER || requester.getRole() == Role.SELLER) {
                final double balance = deposit.getAmount();
                final String description = deposit.getDescription();
                final String successUrl = deposit.getReturnSuccess();
                final String cancelUrl = deposit.getReturnError();

                String currentTime = String.valueOf(new Date().getTime());
                long depositCode = Long.parseLong(currentTime.substring(currentTime.length() - 6));

                ItemData itemData = ItemData.builder()
                        .name("Nạp tiền vào ví của chủ tài khoản " + requester.getFullName())
                        .price(0)
                        .price((int) balance)
                        .quantity(1)
                        .build();

                PaymentData paymentData = PaymentData.builder()
                        .orderCode(depositCode)
                        .description(description)
                        .amount((int) balance)
                        .item(itemData)
                        .returnUrl(successUrl)
                        .cancelUrl(cancelUrl)
                        .build();

                // Tạo liên kết thanh toán
                CheckoutResponseData paymentLinkData = payOS.createPaymentLink(paymentData);

                // Tạo giao dịch mà không cập nhật số dư ngay
                TransactionWallet transactionWallet = new TransactionWallet();
                transactionWallet.setAmount((int) balance);
                transactionWallet.setTransactionType(TransactionType.DEPOSIT);
                transactionWallet.setCommissionRate(0);
                transactionWallet.setCommissionAmount(0);
                transactionWallet.setTransactionStatus(TransactionStatus.PENDING); // Trạng thái ban đầu là PENDING
                transactionWallet.setTransactionWalletCode(paymentLinkData.getOrderCode());
                transactionWallet.setWalletCustomer(getOrCreateWallet(requester)); // Tạo hoặc lấy ví của người dùng

                transactionWalletRepository.save(transactionWallet);

                // Trả về link thanh toán cho người dùng
                response.put("error", 0);
                response.put("message", "Payment link created successfully");
                response.set("data", mapper.valueToTree(paymentLinkData));
                return ResponseEntity.ok(new ResponseObject("Payment link created", HttpStatus.OK, paymentLinkData));

            } else {
                response.put("error", 1);
                response.put("message", "Unauthorized role for deposit");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ResponseObject("Unauthorized role", HttpStatus.FORBIDDEN, null));
            }
        } catch (Exception e) {
            response.put("error", -1);
            response.put("message", "An error occurred: " + e.getMessage());
            response.set("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject("An error occurred", HttpStatus.INTERNAL_SERVER_ERROR, null));
        }
    }

    // Phương thức để tạo hoặc lấy ví của người dùng
    private WalletCustomer getOrCreateWallet(User requester) {
        return walletCustomerRepository.findByUserId(requester.getId())
                .orElseGet(() -> {
                    WalletCustomer wallet = WalletCustomer.builder()
                            .statusWallet(StatusWallet.ACTIVE)
                            .user(requester)
                            .build();
                    return walletCustomerRepository.save(wallet);
                });
    }







    @Override
    public ResponseEntity<ResponseObject> getWalletCustomer(Long id) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode response = objectMapper.createObjectNode();

        try {
            PaymentLinkData order = payOS.getPaymentLinkInformation(id);
            String authHeader = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
                    .getRequest().getHeader("Authorization");

            // Check for Authorization header
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ResponseObject.builder()
                                .status(HttpStatus.UNAUTHORIZED)
                                .message("Missing or invalid Authorization header")
                                .build());
            }
            String token = authHeader.substring(7);
            String userEmail = jwtService.extractUserEmail(token);
            User requester = userRepository.findByEmailAndStatusIsTrue(userEmail).orElse(null);
            if (requester == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                        .status(HttpStatus.NOT_FOUND)
                        .message("Unauthorized request - User not found")
                        .data(null)
                        .build());
            }

            // Retrieve the transaction wallet
            var transactionWallet = transactionWalletRepository.findTransactionWalletByTransactionWalletCode(id).orElse(null);
            if (transactionWallet == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                        .status(HttpStatus.NOT_FOUND)
                        .message("Transaction wallet not found")
                        .data(null)
                        .build());
            }

            WalletCustomer walletCustomer = walletCustomerRepository.findWalletCustomerByUser_Id(requester.getId()).orElse(null);
            if (walletCustomer == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseObject("Wallet not found", HttpStatus.NOT_FOUND, response));
            }

            // Call the new method to process the transaction status
            return processTransactionStatus(order.getStatus(), order.getAmount(), walletCustomer, transactionWallet, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", -1);
            response.put("message", e.getMessage());
            response.set("data", null);
            return ResponseEntity.ok().body(new ResponseObject("Transaction Failed", HttpStatus.BAD_REQUEST, response));
        }
    }


    private ResponseEntity<ResponseObject> processTransactionStatus(String orderStatus, double amount, WalletCustomer walletCustomer, TransactionWallet transactionWallet, ObjectNode response) {
        switch (orderStatus) {
            case "PAID":
                // Update wallet balance
                walletCustomer.setBalance(walletCustomer.getBalance() + amount);
                walletCustomerRepository.save(walletCustomer);

                // Update transaction details
                transactionWallet.setTransactionType(TransactionType.DEPOSIT);
                transactionWallet.setTransactionStatus(TransactionStatus.COMPLETED);
                transactionWallet.setWalletCustomer(walletCustomer);
                transactionWalletRepository.save(transactionWallet);

                JsonNode data = response.set("data", ObjectMapper.valueToTree(order));
                response.put("error", 0);
                response.put("message", "Transaction successful. Amount credited to wallet.");
                break;

            case "PENDING":
                response.put("error", 1);
                response.put("message", "Transaction is pending. No action taken.");
                break;

            case "PROCESSING":
                response.put("error", 2);
                response.put("message", "Transaction is being processed. Please wait.");
                break;

            case "CANCELLED":
                transactionWallet.setTransactionStatus(TransactionStatus.CANCELLED);
                transactionWalletRepository.save(transactionWallet);
                response.put("error", 3);
                response.put("message", "Transaction has been cancelled.");
                break;

            default:
                response.put("error", -1);
                response.put("message", "Unknown transaction status.");
                break;
        }

        return ResponseEntity.ok().body(new ResponseObject("Transaction processed", HttpStatus.OK, response));
    }



}