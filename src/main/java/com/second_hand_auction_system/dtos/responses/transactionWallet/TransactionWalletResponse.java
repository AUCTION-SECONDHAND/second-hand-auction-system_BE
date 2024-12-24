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
    private Integer transactionId;

    private TransactionType transactionType;

    private long amount;

    private long transactionWalletCode;

    private long netAmount;

    private long oldAmount;

    private TransactionStatus transactionStatus;

    private String senderName;

    private String recipientName;

    private LocalDateTime transactionDate;

    private String description;

    private String image;

}
