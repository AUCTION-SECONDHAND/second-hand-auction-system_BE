package com.second_hand_auction_system.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "auction_type")
public class AuctionType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer auctionTypeId;

    @Column(name = "auction_type_name")
    private String auctionTypeName;

    @Column(name = "auction_type_description")
    private String auctionTypeDescription;
}
