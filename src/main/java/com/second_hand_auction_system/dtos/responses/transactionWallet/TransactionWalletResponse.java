package com.second_hand_auction_system.dtos.responses.transactionWallet;

import com.second_hand_auction_system.utils.TransactionStatus;
import com.second_hand_auction_system.utils.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class TransactionWalletResponse {
    @Positive(message = "Transaction ID must be a positive number")
    private Integer transactionId;

    @NotNull(message = "Transaction type cannot be null")
    private TransactionType transactionType;

    @PositiveOrZero(message = "Amount must be zero or positive")
    private long amount;

    @Positive(message = "Transaction wallet code must be a positive number")
    private long transactionWalletCode;

    @NotNull(message = "Transaction status cannot be null")
    private TransactionStatus transactionStatus;

    @NotBlank(message = "Sender name cannot be blank")
    private String senderName;

    @NotBlank(message = "Recipient name cannot be blank")
    private String recipientName;

    @NotNull(message = "Transaction date cannot be null")
    private LocalDateTime transactionDate;
}
