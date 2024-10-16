package com.second_hand_auction_system.dtos.responses.address;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddressResponse {

    private Integer addressId;
    private String district_code;
    private String district_name;
    private String address_name;
    private String default_address;
    private String last_name;
    private String phone_number;
    private String province;
    private String province_name;
    private boolean status;
    private String street_address;
    private String ward_code;
    private String ward_name;
    private Integer userId;
}
