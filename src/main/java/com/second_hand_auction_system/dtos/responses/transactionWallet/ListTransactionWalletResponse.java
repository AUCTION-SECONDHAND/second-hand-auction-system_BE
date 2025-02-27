package com.second_hand_auction_system.dtos.responses.transactionWallet;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ListTransactionWalletResponse {
    private List<TransactionWalletResponse> transactionWallet;
}
