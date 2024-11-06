package com.second_hand_auction_system.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "item_specific")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ItemSpecific extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer itemSpecificId;

    @Column(name = "percent")
    private Double percent;

    @Column(name = "type")
    private String type;

    @Column(name = "color")
    private String color;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "dimension")
    private String dimension;

    @Column(name = "original")
    private String original;

    @Column(name = "manufacture_date")
    private Date manufactureDate;

    @Column(name = "material")
    private String material;

    @Column(name = "price_buy_now")
    private Double priceBuyNow;

    @OneToOne
    @JoinColumn(name = "item_id")
    private Item item;

}
