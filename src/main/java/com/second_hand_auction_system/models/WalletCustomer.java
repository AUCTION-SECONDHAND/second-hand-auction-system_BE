package com.second_hand_auction_system.models;

import com.second_hand_auction_system.utils.StatusWallet;
import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "wallet_customer")
public class WalletCustomer extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer walletCustomerId;

    @Column(name = "balance")
    private double balance;

    @Enumerated(EnumType.STRING)
    private StatusWallet statusWallet;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
