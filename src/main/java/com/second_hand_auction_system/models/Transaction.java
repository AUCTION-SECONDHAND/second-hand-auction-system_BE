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
@Table(name = "transaction")
public class Transaction extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer transactionWalletId;

    @Column(name = "transaction_code")
    private long transactionWalletCode;

    @Column(name = "amount")
    private long amount;

    @Column(name = "commission_amount")
    private int commissionAmount;

    @Column(name = "commission_rate")
    private double commissionRate;

    @Column(name = "description")
    private String description;

    @Column(name = "old_Amount" )
    private double oldAmount;

    @Column(name = "net_Amount")
    private double netAmount;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "recipient_name")
    private String recipient;

    @Column(name = "sender_name")
    private String sender;

    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus;

    @Column(name = "image", length = 255, nullable = true)
    private String image;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;


    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type")
    private TransactionType transactionType;
}
