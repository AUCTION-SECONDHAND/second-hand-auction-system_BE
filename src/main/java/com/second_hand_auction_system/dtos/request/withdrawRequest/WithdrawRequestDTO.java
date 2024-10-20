package com.second_hand_auction_system.dtos.request.withdrawRequest;

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

public class WithdrawRequestDTO {

    @NotNull(message = "Request amount is required")
    @Positive(message = "Request amount must be a positive value")
    private double requestAmount;

    @NotNull(message = "Bank account is required")
    private String bankAccount;

    // Assuming bank number is optional but you might want to validate it
    @Size(min = 10, max = 20, message = "Bank number should be between 10 to 20 digits")
    private String bankNumber;

    @Size(max = 255, message = "Note should not exceed 255 characters")
    private String note;

    @NotNull(message = "Process date is required")
    @FutureOrPresent(message = "Process date cannot be in the past")
    private LocalDateTime processAt;

    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;

    // Uncomment this if you want to validate the wallet customer ID
//    @NotNull(message = "Wallet customer ID is required")
//    @Positive(message = "Wallet customer ID must be a positive integer")
//    private Integer walletCustomer;
}
