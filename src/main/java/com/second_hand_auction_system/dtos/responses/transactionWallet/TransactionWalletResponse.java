package com.second_hand_auction_system.dtos.responses.transactionWallet;

import com.second_hand_auction_system.utils.TransactionStatus;
import com.second_hand_auction_system.utils.TransactionType;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class TransactionWalletResponse {
    private String transactionId;
    private TransactionType transactionType;
    private int amount;
    private long transactionWalletCode;
    private TransactionStatus transactionStatus;
    private int walletCustomerId;
}
