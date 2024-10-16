package com.second_hand_auction_system.service.address;

import com.second_hand_auction_system.dtos.request.address.AddressDto;

import java.util.List;

public interface IAddressService {
  AddressDto createAddress(AddressDto addressDto) throws Exception;

  AddressDto updateAddress(Integer addressId, AddressDto addressDto) throws Exception;

  AddressDto getAddressById(Integer addressId) throws Exception;
  List<AddressDto> getAllAddress(Integer userId) throws Exception;
  void deleteAddress(Integer addressId) throws Exception;

}
