package com.second_hand_auction_system.service.withdrawRequest;

import com.second_hand_auction_system.dtos.request.withdrawRequest.WithdrawRequestDTO;
import com.second_hand_auction_system.dtos.responses.ResponseObject;
import com.second_hand_auction_system.dtos.responses.withdraw.WithdrawResponse;
import com.second_hand_auction_system.models.User;
import com.second_hand_auction_system.models.WalletCustomer;
import com.second_hand_auction_system.models.WithdrawRequest;
import com.second_hand_auction_system.repositories.UserRepository;
import com.second_hand_auction_system.repositories.WalletCustomerRepository;
import com.second_hand_auction_system.repositories.WithdrawRequestRepository;
import com.second_hand_auction_system.service.jwt.JwtService;
import com.second_hand_auction_system.service.user.UserService;
import com.second_hand_auction_system.utils.RequestStatus;
import com.second_hand_auction_system.utils.Role;
import com.second_hand_auction_system.utils.TransactionType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class WithdrawRequestService implements IWithdrawRequestService {
    private final WithdrawRequestRepository withdrawRequestRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final WalletCustomerRepository walletCustomerRepository;
    private final ModelMapper modelMapper;

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

        WalletCustomer walletCustomer = walletCustomerRepository.findByWalletCustomerId(requester.getId()).orElse(null);

        WithdrawRequest withdrawRequest1 = WithdrawRequest.builder()
                .requestAmount(withdrawRequest.getRequestAmount())
                .requestStatus(RequestStatus.PENDING)
                .processAt(withdrawRequest.getProcessAt())
                .note(withdrawRequest.getNote())
                .walletCustomer(walletCustomer)
                .build();

        withdrawRequestRepository.save(withdrawRequest1);

        WithdrawResponse withdrawResponse = WithdrawResponse.builder()
                .requestAmount(withdrawRequest1.getRequestAmount())
                .requestStatus(withdrawRequest1.getRequestStatus())
                .note(withdrawRequest1.getNote())
                .processAt(withdrawRequest1.getProcessAt())
                .transactionType(TransactionType.WITHDRAWAL)
                .walletCustomer(walletCustomer.getWalletCustomerId())
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder()
                .status(HttpStatus.OK)
                .message("Request successful")
                .data(withdrawResponse)
                .build());
    }

}
