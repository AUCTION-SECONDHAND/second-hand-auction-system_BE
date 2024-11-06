package com.second_hand_auction_system.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "deposite_user")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class AuctionRegistrationUser extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    //@Enumerated(EnumType.STRING)
    @JoinColumn(name = "status_registration")
    private Boolean statusRegistration;


    @ManyToOne
    @JoinColumn(name = "auction_registration_id")
    private AuctionRegistration auctionRegistration;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

}
