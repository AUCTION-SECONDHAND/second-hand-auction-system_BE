package com.second_hand_auction_system.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "wallet_system")
public class WalletSystem extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer walletAdminId;

    @Column(name = "balance")
    private double balance;

    @ManyToOne
    @JoinColumn(name = "transaction_system_id")
    private TransactionSystem transactionSystem;
}
