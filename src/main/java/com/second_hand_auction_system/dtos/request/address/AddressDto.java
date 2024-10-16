package com.second_hand_auction_system.dtos.request.address;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddressDto {
    @NotBlank(message = "District code is required")
    private String district_code;

    @NotBlank(message = "District name is required")
    private String district_name;

    @NotBlank(message = "Address name is required")
    private String address_name;

    private String default_address;

    @NotBlank(message = "Last name is required")
    private String last_name;

    @NotBlank(message = "Phone number is required")
    private String phone_number;

    @NotBlank(message = "Province is required")
    private String province;

    private String province_name;

    @NotNull(message = "Status is required")
    private boolean status;

    private String street_address;

    @NotBlank(message = "Ward code is required")
    private String ward_code;

    private String ward_name;

    @NotNull(message = "User ID is required")
    @JsonProperty
    private Integer userId;
}


