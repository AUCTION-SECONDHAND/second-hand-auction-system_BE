package com.second_hand_auction_system.models;

import com.second_hand_auction_system.utils.TransactionStatus;
import com.second_hand_auction_system.utils.TransactionType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "transaction_wallet")
public class TransactionWallet extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer transactionWalletId;

    @Column(name = "transaction_wallet_code")
    private long transactionWalletCode;

    @Column(name = "amount")
    private int amount;

    @Column(name = "commission_amount")
    private int commissionAmount;

    @Column(name = "commission_rate")
    private int commissionRate;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus;

    @Column(name = "image")
    private String image;

    @ManyToOne
    @JoinColumn(name = "wallet_customer_id")
    private WalletCustomer walletCustomer;

    @ManyToOne
    @JoinColumn(name = "wallet_id")
    private WalletSystem walletSystem;


}
