package com.second_hand_auction_system.converters.address;

import com.second_hand_auction_system.dtos.request.address.AddressDto;
import com.second_hand_auction_system.dtos.responses.address.AddressResponse;
import com.second_hand_auction_system.models.Address;
import com.second_hand_auction_system.models.User;

public class AddressConverter {
    // Converter AddressDto => Address
    public static Address convertToEntity(AddressDto addressDto, User user) {
        Address address = new Address();
        address.setDistrict_code(addressDto.getDistrict_code());
        address.setDistrict_name(addressDto.getDistrict_name());
        address.setAddress_name(addressDto.getAddress_name());
        address.setDefault_address(addressDto.getDefault_address());
        address.setLast_name(addressDto.getLast_name());
        address.setPhone_number(addressDto.getPhone_number());
        address.setProvince(addressDto.getProvince());
        address.setProvince_name(addressDto.getProvince_name());
        address.setStatus(addressDto.isStatus());
        address.setStreet_address(addressDto.getStreet_address());
        address.setWard_code(addressDto.getWard_code());
        address.setWard_name(addressDto.getWard_name());

        address.setUser(user);
        return address;
    }

    // Converter Address => AddressResponse
    public static AddressResponse convertToResponse(Address address) {
        return AddressResponse.builder()
                .addressId(address.getAddressId())
                .district_code(address.getDistrict_code())
                .district_name(address.getDistrict_name())
                .address_name(address.getAddress_name())
                .default_address(address.getDefault_address())
                .last_name(address.getLast_name())
                .phone_number(address.getPhone_number())
                .province(address.getProvince())
                .province_name(address.getProvince_name())
                .status(address.isStatus())
                .street_address(address.getStreet_address())
                .ward_code(address.getWard_code())
                .ward_name(address.getWard_name())
                .userId(address.getUser().getId())
                .build();
    }
}
