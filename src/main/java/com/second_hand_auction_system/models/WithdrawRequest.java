package com.second_hand_auction_system.models;

import com.second_hand_auction_system.utils.PaymentMethod;
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

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Column(name = "process_at")
    private LocalDateTime processAt;

    @Column(name = "bank_account")
    private String bankAccount;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "account_number")
    private String accountNumber;

    @ManyToOne
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;
}
