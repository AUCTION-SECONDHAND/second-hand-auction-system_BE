package com.second_hand_auction_system.models;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "item_specification")
@Entity
public class ItemSpecification extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer itemSpecificationId;

    @Column(name = "cpu")
    private String cpu;

    @Column(name = "ram")
    private String ram;

    @Column(name = "screen_size")
    private String screenSize;

    @Column(name = "camera_specs")
    private String cameraSpecs;

    @Column(name = "connectivity")
    private String connectivity;

    @Column(name = "sensors")
    private String sensors;

    @OneToOne(mappedBy = "itemSpecification", cascade = CascadeType.ALL)
    private Item item;


}
