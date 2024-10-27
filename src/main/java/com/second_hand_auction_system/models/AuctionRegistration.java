package com.second_hand_auction_system.models;

import com.second_hand_auction_system.utils.Registration;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Builder
@Table(name = "auction_registration")
public class AuctionRegistration extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer auctionRegistrationId;

    @Column(name = "deposite_amount")
    private double depositeAmount;

    @Enumerated(EnumType.STRING)
    private Registration registration;

    @ManyToMany
    @JoinTable(
            name = "user_auction_registration",
            joinColumns = @JoinColumn(name = "auction_registration_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> users = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "auction_id")
    private Auction auction;
}
