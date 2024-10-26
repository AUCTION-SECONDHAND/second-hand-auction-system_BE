package com.second_hand_auction_system.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "bid")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Bid extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer bidId;

    @Column(name = "bid_amout")
    private int bidAmount;

    @Column(name = "bid_time")
    private LocalDateTime bidTime;

//    @Column(name = "bid_status")
//    private String bidStatus;

    @Column(name = "win_bid")
    private boolean winBid;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id")
    private Auction auction;
}
