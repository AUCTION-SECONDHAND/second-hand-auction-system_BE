package com.second_hand_auction_system.dtos.responses.withdraw;

import com.second_hand_auction_system.utils.RequestStatus;
import com.second_hand_auction_system.utils.TransactionType;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class WithdrawResponse {

    private double requestAmount;

    private RequestStatus requestStatus;

    private String note;
    private String bankAccount;
    private LocalDateTime processAt;
    private TransactionType transactionType;
    private String accountNumber;
    private Integer walletCustomer;
}
