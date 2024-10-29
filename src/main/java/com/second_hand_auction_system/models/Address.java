package com.second_hand_auction_system.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "address")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Data
public class Address extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer addressId;

    @Column(name = "district_code")
    private String district_code;

    @Column(name = "district_name")
    private String district_name;

    @Column(name = "address_name")
    private String address_name;

    @Column(name = "default_address")
    private String default_address;

    @Column(name = "last_name")
    private String last_name;

    @Column(name = "phone_number")
    private String phone_number;

    @Column(name = "province")
    private String province;

    @Column(name = "province_name")
    private String province_name;

    @Column(name = "status")
    private boolean status;

    @Column(name = "street_address")
    private String street_address;

    @Column(name = "ward_code")
    private String ward_code;

    @Column(name = "ward_name")
    private String ward_name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
