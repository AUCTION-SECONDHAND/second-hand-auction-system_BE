package com.second_hand_auction_system.dtos.responses.item;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemSpecificationResponse {
    private String cpu;
    private String ram;
    private String screenSize;
    private String cameraSpecs;
    private String connectivity;
    private String sensors;

    @JsonProperty("item_spec_Id")
    private Integer itemSpecificationId;



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
