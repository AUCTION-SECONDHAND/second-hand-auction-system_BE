package com.second_hand_auction_system.dtos.responses.transactionWallet;

import com.second_hand_auction_system.utils.TransactionStatus;
import com.second_hand_auction_system.utils.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class TransactionWalletResponse {
    @Positive(message = "Transaction ID must be a positive number")
    private int transactionId;

    @NotNull(message = "Transaction type cannot be null")
    private TransactionType transactionType;

    @PositiveOrZero(message = "Amount must be zero or positive")
    private int amount;

    @Positive(message = "Transaction wallet code must be a positive number")
    private long transactionWalletCode;

    @NotNull(message = "Transaction status cannot be null")
    private TransactionStatus transactionStatus;

    @NotBlank(message = "Wallet customer name cannot be blank")
    private String walletCustomerName;

    @NotNull(message = "Transaction date cannot be null")
    private LocalDateTime transactionDate;
}
