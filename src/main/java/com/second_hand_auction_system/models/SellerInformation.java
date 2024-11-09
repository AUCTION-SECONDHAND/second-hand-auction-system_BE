package com.second_hand_auction_system.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seller_information")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class SellerInformation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer sellerId;

    @Column(name = "store_name")
    private String storeName;

    @Column(name = "address")
    private String address;

    @Column(name = "description", columnDefinition = "LONGTEXT")
    private String description;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "background_image")
    private String backgroundImage;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}