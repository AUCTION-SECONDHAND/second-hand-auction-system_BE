package com.second_hand_auction_system.service.withdrawRequest;

import com.second_hand_auction_system.dtos.request.withdrawRequest.WithdrawApprove;
import com.second_hand_auction_system.dtos.request.withdrawRequest.WithdrawRequestDTO;
import com.second_hand_auction_system.dtos.responses.ResponseObject;
import com.second_hand_auction_system.dtos.responses.withdraw.WithdrawResponse;
import com.second_hand_auction_system.models.User;
import com.second_hand_auction_system.models.Wallet;
import com.second_hand_auction_system.models.WithdrawRequest;
import com.second_hand_auction_system.repositories.UserRepository;
import com.second_hand_auction_system.repositories.WalletRepository;
import com.second_hand_auction_system.repositories.WithdrawRequestRepository;
import com.second_hand_auction_system.service.VNPay.VNPAYService;
import com.second_hand_auction_system.service.jwt.JwtService;
import com.second_hand_auction_system.utils.PaymentMethod;
import com.second_hand_auction_system.utils.RequestStatus;
import com.second_hand_auction_system.utils.Role;
import com.second_hand_auction_system.utils.TransactionType;
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

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WithdrawRequestService implements IWithdrawRequestService {
    private final WithdrawRequestRepository withdrawRequestRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
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

                .map(withdrawRequest -> WithdrawResponse.builder()
                        .requestAmount(withdrawRequest.getRequestAmount())
                        .requestStatus(withdrawRequest.getRequestStatus())
                        .note(withdrawRequest.getNote())
                        .bankAccount(withdrawRequest.getBankAccount())
                        .processAt(withdrawRequest.getProcessAt())
                        .paymentMethod(withdrawRequest.getPaymentMethod())
                        .accountNumber(withdrawRequest.getAccountNumber())
                        .walletCustomerId(withdrawRequest.getWallet().getWalletId())
                        .sellerName(withdrawRequest.getWallet().getUser().getFullName())
                        .avtar(withdrawRequest.getWallet().getUser().getAvatar())
                        .build())
                .toList();
        Map<String,Object> map = new HashMap<>();
        map.put("data",withdrawResponses);
        map.put("totalPages",withdrawRequestPage.getTotalPages());
        map.put("totalElements",withdrawRequestPage.getTotalElements());

        return ResponseEntity.ok(ResponseObject.builder()
                .status(HttpStatus.OK)
                .data(map)
                .message("All requests successful")
                .build());
    }


}
