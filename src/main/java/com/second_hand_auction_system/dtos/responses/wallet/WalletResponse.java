package com.second_hand_auction_system.dtos.responses.wallet;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class WalletResponse {
    private String paymentUrl;
    private Integer transactionId;
}
