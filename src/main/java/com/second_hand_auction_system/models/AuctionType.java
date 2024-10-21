package com.second_hand_auction_system.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "auction_type")
public class AuctionType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer auctionTypeId;

    @Column(name = "auction_type_name")
    private String auctionTypeName;
}
