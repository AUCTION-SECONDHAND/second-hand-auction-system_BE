package com.second_hand_auction_system.models;

import com.second_hand_auction_system.utils.Registration;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashMap;
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

    @Column(name = "registrantion")
    private Boolean registration;

    @Column(name="number_participant")
    private int numberParticipant;

    @ManyToMany
    @JoinTable(
            name = "deposite_user",
            joinColumns = @JoinColumn(name = "auction_registration_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> users;

    @ManyToOne
    @JoinColumn(name = "auction_id")
    private Auction auction;
}
