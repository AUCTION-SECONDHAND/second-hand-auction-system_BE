package com.second_hand_auction_system.service.withdrawRequest;

import com.second_hand_auction_system.configurations.VNPayConfig;
import com.second_hand_auction_system.dtos.request.walletCustomer.Deposit;
import com.second_hand_auction_system.dtos.request.withdrawRequest.WithdrawApprove;
import com.second_hand_auction_system.dtos.request.withdrawRequest.WithdrawRequestDTO;
import com.second_hand_auction_system.dtos.responses.ResponseObject;
import com.second_hand_auction_system.dtos.responses.wallet.WalletResponse;
import com.second_hand_auction_system.dtos.responses.withdraw.WithdrawResponse;
import com.second_hand_auction_system.models.Transaction;
import com.second_hand_auction_system.models.User;
import com.second_hand_auction_system.models.Wallet;
import com.second_hand_auction_system.models.WithdrawRequest;
import com.second_hand_auction_system.repositories.TransactionRepository;
import com.second_hand_auction_system.repositories.UserRepository;
import com.second_hand_auction_system.repositories.WalletRepository;
import com.second_hand_auction_system.repositories.WithdrawRequestRepository;
import com.second_hand_auction_system.service.VNPay.VNPAYService;
import com.second_hand_auction_system.service.jwt.JwtService;
import com.second_hand_auction_system.utils.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class WithdrawRequestService implements IWithdrawRequestService {
    private final WithdrawRequestRepository withdrawRequestRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final ModelMapper modelMapper;
    private final VNPAYService vnpayService;

    @Override
    @Transactional
    public ResponseEntity<?> requestWithdraw(WithdrawRequestDTO withdrawRequest) {
        String authHeader = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest().getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResponseObject.builder()
                    .status(HttpStatus.UNAUTHORIZED)
                    .message("Unauthorized")
                    .build());
        }

        String token = authHeader.substring(7);
        String email = jwtService.extractUserEmail(token);
        User requester = userRepository.findByEmail(email).orElse(null);
        if (requester == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                    .status(HttpStatus.NOT_FOUND)
                    .data(null)
                    .message("User not found")
                    .build());
        }

        if (!requester.getRole().equals(Role.SELLER)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResponseObject.builder()
                    .status(HttpStatus.FORBIDDEN)
                    .data(null)
                    .message("You don't have permission to withdraw request")
                    .build());
        }

        Wallet wallet = walletRepository.findByWalletId(requester.getId()).orElse(null);

        WithdrawRequest withdrawRequest1 = WithdrawRequest.builder()
                .requestAmount(withdrawRequest.getRequestAmount())
                .requestStatus(RequestStatus.PENDING)
                .processAt(LocalDateTime.now())
                .paymentMethod(PaymentMethod.WALLET_PAYMENT)
                .note(withdrawRequest.getNote())
                .bankAccount(withdrawRequest.getBankAccount())
                .accountNumber(withdrawRequest.getBankNumber())
                .wallet(wallet)
                .bankName(withdrawRequest.getBankName())
                .build();

        withdrawRequestRepository.save(withdrawRequest1);

        assert wallet != null;
        WithdrawResponse withdrawResponse = WithdrawResponse.builder()
                .requestAmount(withdrawRequest1.getRequestAmount())
                .requestStatus(withdrawRequest1.getRequestStatus())
                .note(withdrawRequest1.getNote())
                .processAt(withdrawRequest1.getProcessAt())
                .paymentMethod(withdrawRequest1.getPaymentMethod())
                .accountNumber(withdrawRequest1.getAccountNumber())
                .bankAccount(withdrawRequest1.getBankAccount())
                .walletCustomerId(wallet.getWalletId())
                .sellerName(requester.getFullName())
                .avtar(requester.getAvatar())
                .bankName(withdrawRequest1.getBankName())
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder()
                .status(HttpStatus.OK)
                .message("Request successful")
                .data(withdrawResponse)
                .build());
    }

    @Override
    public ResponseEntity<?> approve(Integer id, WithdrawApprove withdrawApprove, HttpServletRequest request) {
        WithdrawRequest withdrawRequest = withdrawRequestRepository.findById(id).orElse(null);
        if (withdrawRequest != null) {
            withdrawRequest.setRequestStatus(withdrawApprove.getStatus());
            withdrawRequestRepository.save(withdrawRequest);
            WithdrawResponse withdrawResponse = WithdrawResponse.builder()
                    .requestAmount(withdrawRequest.getRequestAmount())
                    .requestStatus(withdrawApprove.getStatus())
                    .note(withdrawRequest.getNote())
                    .processAt(withdrawRequest.getProcessAt())
                    .bankAccount(withdrawRequest.getBankAccount())
                    .accountNumber(withdrawRequest.getBankAccount())
                    .paymentMethod(withdrawRequest.getPaymentMethod())
                    .walletCustomerId(withdrawRequest.getWallet().getWalletId())
                    .build();
            return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder()
                    .status(HttpStatus.OK)
                    .message("Withdrawal approved, redirecting to payment")
                    .data(withdrawResponse)
                    .build());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                .status(HttpStatus.NOT_FOUND)
                .message("Withdrawal request not found")
                .build());
    }

    @Override
    public ResponseEntity<?> getAll(int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit, Sort.by(Sort.Order.desc("processAt")));
        Page<WithdrawRequest> withdrawRequestPage = withdrawRequestRepository.findAll(pageable);
        List<WithdrawResponse> withdrawResponses = withdrawRequestPage.stream()
                .map(withdrawRequest -> {
                    User user = withdrawRequest.getWallet() != null ? withdrawRequest.getWallet().getUser() : null;
                    return WithdrawResponse.builder()
                            .bankName(withdrawRequest.getBankName())
                            .withdrawId(withdrawRequest.getWithdrawRequestId())
                            .requestAmount(withdrawRequest.getRequestAmount())
                            .requestStatus(withdrawRequest.getRequestStatus())
                            .note(withdrawRequest.getNote())
                            .bankAccount(withdrawRequest.getBankAccount())
                            .processAt(withdrawRequest.getProcessAt())
                            .paymentMethod(withdrawRequest.getPaymentMethod())
                            .accountNumber(withdrawRequest.getAccountNumber())
                            .walletCustomerId(withdrawRequest.getWallet() != null ? withdrawRequest.getWallet().getWalletId() : null)
                            .sellerName(user != null ? user.getFullName() : "Unknown User")
                            .avtar(user != null ? user.getAvatar() : "default-avatar.png")
                            .build();
                })
                .toList();

        Map<String, Object> map = new HashMap<>();
        map.put("data", withdrawResponses);
        map.put("totalPages", withdrawRequestPage.getTotalPages());
        map.put("totalElements", withdrawRequestPage.getTotalElements());

        return ResponseEntity.ok(ResponseObject.builder()
                .status(HttpStatus.OK)
                .data(map)
                .message("All requests successful")
                .build());
    }

    @Override
    public ResponseEntity<ResponseObject> transfer(Deposit deposit, Integer withdrawId) {
        String authHeader = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest().getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResponseObject.builder()
                    .status(HttpStatus.UNAUTHORIZED)
                    .message("Unauthorized")
                    .build());
        }
        String token = authHeader.substring(7);
        String email = jwtService.extractUserEmail(token);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        var requester = userRepository.findByEmail(email).orElse(null);
        if (requester == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                    .status(HttpStatus.NOT_FOUND)
                    .message("User not found")
                    .data(null)
                    .build());
        }
        var withdrawRequest = withdrawRequestRepository.findById(withdrawId).orElse(null);
        if (withdrawRequest == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                    .status(HttpStatus.NOT_FOUND)
                    .message("Withdrawal request not found")
                    .data(null)
                    .build());
        }

        WalletResponse walletResponse = withdraw(deposit.getAmount(),deposit.getDescription());
        withdrawRequest.setRequestStatus(RequestStatus.ACCEPTED);
        withdrawRequestRepository.save(withdrawRequest);
        return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder()
                        .data(walletResponse)
                        .message("Chuyen tien thanh cong")
                        .status(HttpStatus.OK)
                .build());
    }

    public WalletResponse withdraw(int amount, String description) {
        String authHeader = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest().getHeader("Authorization");
        // Kiểm tra Authorization header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Unauthorized");
        }
        String token = authHeader.substring(7);
        String userEmail = jwtService.extractUserEmail(token);

        // Kiểm tra người dùng
        User requester = userRepository.findByEmailAndStatusIsTrue(userEmail).orElse(null);
        if (requester == null) {
            throw new RuntimeException("User not found");
        }
        // Kiểm tra ví của người dùng
        Wallet wallet = walletRepository.findByUserId(requester.getId()).orElse(null);
        String orderInfo = description;
        String vnp_Version = "2.1.0";
        String bankCode = "NCB";
        String vnp_Command = "pay";
        String vnp_TxnRef = VNPayConfig.getRandomNumber(8);
        String vnp_IpAddr = "127.0.0.1";
        String vnp_TmnCode = VNPayConfig.vnp_TmnCode;
        String orderType = "order-type";

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount*100));
        vnp_Params.put("vnp_CurrCode", "VND");
        if (bankCode != null && !bankCode.isEmpty()) {
            vnp_Params.put("vnp_BankCode", bankCode);
        }
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", orderInfo);
        vnp_Params.put("vnp_OrderType", orderType);

        String locate = "vn";
        vnp_Params.put("vnp_Locale", locate);

//         += VNPayConfig.vnp_ReturnUrl;
        vnp_Params.put("vnp_ReturnUrl", VNPayConfig.vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List fieldNames = new ArrayList(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                try {
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    //Build query
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = VNPayConfig.hmacSHA512(VNPayConfig.secretKey, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = VNPayConfig.vnp_PayUrl + "?" + queryUrl;
        var walletAdmin = walletRepository.findWalletByWalletType(WalletType.ADMIN).orElse(null);
        assert walletAdmin != null;
        var walletCustomer = walletRepository.findByUserId(requester.getId()).orElse(null);
        Transaction transaction1;

        if (walletCustomer == null) {
            // Nếu ví khách hàng không tồn tại
            throw new IllegalStateException("Ví khách hàng không tồn tại.");
        } else {
            if (walletCustomer.getBalance() < amount) {
                // Kiểm tra số dư đủ để rút tiền
                throw new IllegalStateException("Số dư ví không đủ để thực hiện giao dịch rút tiền.");
            }

            // Trừ tiền từ ví khách hàng
            walletCustomer.setBalance(walletCustomer.getBalance() - amount);
            walletRepository.save(walletCustomer);

            // Cộng tiền vào ví admin (nếu cần)
            walletAdmin.setBalance(walletAdmin.getBalance() + amount);
            walletRepository.save(walletAdmin);

            // Tạo giao dịch rút tiền
            transaction1 = Transaction.builder()
                    .transactionType(TransactionType.WITHDRAWAL) // Giao dịch rút tiền
                    .oldAmount(walletCustomer.getBalance() - amount)
                    .netAmount(walletCustomer.getBalance()) // Số dư sau khi rút
                    .amount(- amount) // Số tiền rút
                    .description(description)
                    .wallet(walletCustomer) // Ví khách hàng
                    .recipient("Khách hàng") // Người nhận
                    .sender("Khách hàng") // Người gửi (cũng chính là khách hàng)
                    .commissionAmount(0) // Có thể thêm logic phí giao dịch nếu cần
                    .commissionRate(0)
                    .transactionStatus(TransactionStatus.COMPLETED) // Đặt trạng thái hoàn thành
                    .build();

            transactionRepository.save(transaction1);
        }


        return new WalletResponse(paymentUrl, transaction1.getTransactionWalletId());
    }


}
