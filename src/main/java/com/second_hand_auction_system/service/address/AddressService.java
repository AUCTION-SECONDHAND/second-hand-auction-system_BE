package com.second_hand_auction_system.service.address;

import com.second_hand_auction_system.converters.address.AddressConverter;
import com.second_hand_auction_system.dtos.request.address.AddressDto;
import com.second_hand_auction_system.models.Address;
import com.second_hand_auction_system.repositories.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService implements IAddressService {

    private final AddressRepository addressRepository;

    @Override
    public AddressDto createAddress(AddressDto addressDto) throws Exception {
        Address address = AddressConverter.convertToEntity(addressDto);
        Address savedAddress = addressRepository.save(address);
        return AddressConverter.convertToDto(savedAddress);
    }

    @Override
    public AddressDto updateAddress(Integer addressId, AddressDto addressDto) throws Exception {
        return null;
    }

    @Override
    public AddressDto getAddressById(Integer addressId) throws Exception {
        return null;
    }

    @Override
    public List<AddressDto> getAllAddress(Integer userId) throws Exception {
        return null;
    }

    @Override
    public void deleteAddress(Integer addressId) throws Exception {

    }
}
