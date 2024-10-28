package com.second_hand_auction_system.service.address;

import com.second_hand_auction_system.dtos.request.address.AddressDto;
import com.second_hand_auction_system.dtos.responses.address.AddressResponse;

import java.util.List;

public interface IAddressService {
  AddressResponse createAddress(AddressDto addressDto) throws Exception;

  AddressResponse updateAddress(Integer addressId, AddressDto addressDto) throws Exception;

  AddressResponse getAddressById(Integer addressId) throws Exception;

  List<AddressResponse> getAllAddress() throws Exception;

  void deleteAddress(Integer addressId) throws Exception;

  AddressResponse setDefaultAddress(Integer addressId) throws Exception;
}
