package com.second_hand_auction_system.models;

import jakarta.persistence.*;
import lombok.*;

import java.awt.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "image_item")
public class ImageItem extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer imageItemId;

    @Column(name = "image_url")
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;
}
