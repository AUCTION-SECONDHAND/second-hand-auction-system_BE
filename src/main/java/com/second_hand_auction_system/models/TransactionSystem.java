package com.second_hand_auction_system.models;

import com.second_hand_auction_system.utils.TransactionStatus;
import com.second_hand_auction_system.utils.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "transaction_system")
public class TransactionSystem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer transactionSystemId;

//    @Column(name = "account_number")
//    private int accountNumber;

    @Column(name = "transaction_type")
    private TransactionType transactionType;

    @Column(name = "transaction_system_code")
    private String transactionSystemCode;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Column(name = "amount")
    private double amount;

    @Column(name = "transaction_time")
    private String transactionTime;

//    @Column(name = "reference")
//    private String reference;

    @Column(name = "description")
    private String description;

    @Column(name = "virtual_account_name")
    private String virtualAccountName;

    @ManyToOne
    @JoinColumn(name = "wallet_system_id")
    private WalletSystem walletSystem;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;
}
