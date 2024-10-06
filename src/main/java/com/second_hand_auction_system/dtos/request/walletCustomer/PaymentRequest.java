package com.second_hand_auction_system.dtos.request.walletCustomer;

import com.second_hand_auction_system.utils.TransactionStatus;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class PaymentRequest {
    private String customerName;
    private TransactionStatus transactionStatus;
}
