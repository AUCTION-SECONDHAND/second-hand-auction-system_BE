package com.second_hand_auction_system.models;

import com.second_hand_auction_system.utils.Registration;
import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "auction_registration")
public class AuctionRegistration extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer auctionRegistrationId;

    @Column(name = "deposite_amount")
    private double depositeAmount;

    @Enumerated(EnumType.STRING)
    private Registration registration;

    @Column(name = "note")
    private String note;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}
