package com.second_hand_auction_system.dtos.responses.transaction;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class TransactionResponse {
    private float balance;
    private long totalTransaction;
    private double totalRevenue;
    private int totalUser;
    private int totalAuction;
    private int totalOrder;
}
