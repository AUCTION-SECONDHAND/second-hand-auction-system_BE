package com.second_hand_auction_system.dtos.request.withdrawRequest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.second_hand_auction_system.utils.TransactionType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
//@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})

public class WithdrawRequestDTO {
    @NotNull(message = "Request amount is required")
    @Positive(message = "Request amount must be a positive value")
    private double requestAmount;

//    @NotNull(message = "Request status is required")
//    private RequestStatus requestStatus;

    @Size(max = 255, message = "Note should not exceed 255 characters")
    private String note;

    @NotNull(message = "Process date is required")
    @FutureOrPresent(message = "Process date cannot be in the past")
    private LocalDateTime processAt;

    @NotNull(message = "Transaction date is required")
    private TransactionType transactionType;

//    @NotNull(message = "Wallet customer ID is required")
//    @Positive(message = "Wallet customer ID must be a positive integer")
//    private Integer walletCustomer;
}
