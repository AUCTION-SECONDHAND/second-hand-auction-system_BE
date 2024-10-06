package com.second_hand_auction_system.service.walletCustomer;

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
import com.second_hand_auction_system.service.jwt.IJwtService;
import com.second_hand_auction_system.utils.StatusWallet;
import com.second_hand_auction_system.utils.TransactionStatus;
import com.second_hand_auction_system.utils.TransactionType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import vn.payos.PayOS;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;
import vn.payos.type.PaymentData;
import vn.payos.type.PaymentLinkData;

import java.util.Date;
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
            final double balance = deposit.getAmount();
            final String description = deposit.getDescription();
            final String successUrl = deposit.getReturnSuccess();
            final String cancelUrl = deposit.getReturnSuccess();
            final String bankName = deposit.getBankName();
            Optional<WalletCustomer> walletOpt = walletCustomerRepository.findByWalletCustomerId(requester.getId());
            WalletCustomer wallet;
            if (walletOpt.isEmpty()) {
                wallet = WalletCustomer.builder()
                        .balance(balance)
                        .statusWallet(StatusWallet.ACTIVE)
                        .lastTransaction(bankName)
                        .user(requester)
                        .build();
                walletCustomerRepository.save(wallet);
            } else {
                wallet = walletOpt.get();
                double newBalance = wallet.getBalance() + balance;
                wallet.setBalance(newBalance);
                wallet.setUser(requester);
                walletCustomerRepository.save(wallet);
            }
            String currentTime = String.valueOf(new Date().getTime());
            long depositCode = Long.parseLong(currentTime.substring(currentTime.length() - 6));

            ItemData itemData = ItemData.builder()
                    .name("Nạp tiền vào ví của chủ tài khoản " + requester.getFullName())
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
            CheckoutResponseData paymentLinkData = payOS.createPaymentLink(paymentData);
            TransactionWallet transactionWallet = new TransactionWallet();
            transactionWallet.setAmount((int) balance);
            transactionWallet.setTransactionType(TransactionType.DEPOSIT);
            transactionWallet.setCommissionRate(0);
            transactionWallet.setCommissionAmount(0);
            transactionWallet.setTransactionStatus(TransactionStatus.PENDING);
            transactionWallet.setTransactionWalletCode(paymentLinkData.getOrderCode());
            transactionWalletRepository.save(transactionWallet);



            response.put("error", 0);
            response.put("message", "success");
            response.set("data", mapper.valueToTree(paymentLinkData));
            return ResponseEntity.ok(new ResponseObject("Deposit success",HttpStatus.valueOf(HttpStatus.OK.value()),paymentLinkData));
        } catch (Exception e) {
            response.put("error", -1);
            response.put("message", "An error occurred: " + e.getMessage());
            response.set("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseObject("An error occurred",HttpStatus.INTERNAL_SERVER_ERROR,null));
        }
    }

    @Override
    public ResponseEntity<ResponseObject> getWalletCustomer(Long id) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode response = objectMapper.createObjectNode();

        try {
            PaymentLinkData order = payOS.getPaymentLinkInformation(id);
            String authHeader = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest().getHeader("Authorization");
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
            WalletCustomer walletCustomer = walletCustomerRepository.findByWalletCustomerId(requester.getId()).orElse(null);
            var transactionWallet = transactionWalletRepository.findTransactionWalletByTransactionWalletCode(id).orElse(null);
            if(transactionWallet == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                                .status(HttpStatus.NOT_FOUND)
                                .message("Not found ")
                                .data(null)
                        .build());
            }

            transactionWallet.setCommissionAmount(0);
            transactionWallet.setCommissionRate(0);
            transactionWallet.setWalletCustomer(walletCustomer);
            transactionWallet.setTransactionType(TransactionType.DEPOSIT);
            transactionWallet.setTransactionStatus(TransactionStatus.COMPLETED);
            transactionWalletRepository.save(transactionWallet);
//            var transactionSystem = transactionSystemRepository.f
            response.set("data", objectMapper.valueToTree(order));
            response.put("error", 0);
            response.put("message", "ok");
            return ResponseEntity.ok().body(new ResponseObject("Transaction success",HttpStatus.OK,response));
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", -1);
            response.put("message", e.getMessage());
            response.set("data", null);
            return ResponseEntity.ok().body(new ResponseObject("Transaction Fail",HttpStatus.BAD_REQUEST,response));
        }

    }

    @Override
    public ResponseEntity<?> updateStatus(PaymentRequest payment) {
        return null;
    }

}
