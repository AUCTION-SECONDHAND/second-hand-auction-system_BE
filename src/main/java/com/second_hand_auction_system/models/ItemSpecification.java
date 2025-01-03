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


    @Column(name = "sim")
    private String sim;

    @Column(name = "sim_slots")
    private Integer simSlots;

    @Column(name = "os")
    private String os;

    @Column(name = "os_family")
    private String osFamily;

    @Column(name = "bluetooth")
    private String bluetooth;

    @Column(name = "usb")
    private String usb;

    @Column(name = "wlan")
    private String wlan;


    @Column(name = "speed")
    private String speed;

    @Column(name = "network_technology")
    private String networkTechnology;


}
