package com.second_hand_auction_system.dtos.responses.withdraw;

import com.second_hand_auction_system.utils.PaymentMethod;
import com.second_hand_auction_system.utils.RequestStatus;
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
    private PaymentMethod paymentMethod;
    private String accountNumber;
    private Integer walletCustomerId;
    private String sellerName;
    private String bankName;
    private String avtar;
}
