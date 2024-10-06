package com.second_hand_auction_system.models;

import com.second_hand_auction_system.utils.RequestStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "with_draw_request")
public class WithdrawRequest extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer withdrawRequestId;

    @Column(name = "request_amount")
    private double requestAmount;

    @Enumerated(EnumType.STRING)
    private RequestStatus requestStatus;

    @Column(name = "note")
    private String note;

    @Column(name = "image")
    private String image;

    @Column(name = "process_at")
    private LocalDateTime processAt;

    @ManyToOne
    @JoinColumn(name = "wallet_customer_id")
    private WalletCustomer walletCustomer;
}
