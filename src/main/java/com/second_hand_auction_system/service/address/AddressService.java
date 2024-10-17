package com.second_hand_auction_system.service.address;

import com.second_hand_auction_system.converters.address.AddressConverter;
import com.second_hand_auction_system.dtos.request.address.AddressDto;
import com.second_hand_auction_system.models.Address;
import com.second_hand_auction_system.models.User;
import com.second_hand_auction_system.repositories.AddressRepository;
import com.second_hand_auction_system.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressService implements IAddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Override
    public AddressDto createAddress(AddressDto addressDto) throws Exception {

        int addressCount = addressRepository.countByUserId(addressDto.getUserId());

        //A user can only have 6 address
        if (addressCount >= 6) {
            throw new Exception("Cannot create a new address. The user already has the maximum number of addresses (6).");
        }

        //Set address default, if it is the first created address
        boolean isFirstAddress = (addressCount == 0);

        //Many to One khởi tạo cái User và chỉ lưu userId vô address
        User user = new User();
        user.setId(addressDto.getUserId());

        Address address = AddressConverter.convertToEntity(addressDto, user);

        address.setStatus(isFirstAddress);

        Address savedAddress = addressRepository.save(address);

        return AddressConverter.convertToDto(savedAddress);
    }





    @Override
    public AddressDto updateAddress(Integer addressId, AddressDto addressDto) throws Exception {
        Optional<Address> existingAddressOpt = addressRepository.findById(addressId);
        if (existingAddressOpt.isPresent()) {
            Address existingAddress = existingAddressOpt.get();

            User user = existingAddress.getUser();

            Address updatedAddress = AddressConverter.convertToEntity(addressDto, user);

            updatedAddress.setAddressId(existingAddress.getAddressId());
            updatedAddress.setCreateAt(existingAddress.getCreateAt());
            updatedAddress.setStatus(existingAddress.isStatus());

            Address savedAddress = addressRepository.save(updatedAddress);

            return AddressConverter.convertToDto(savedAddress);
        } else {
            throw new Exception("Address not found with ID: " + addressId);
        }
    }


    @Override
    public AddressDto getAddressById(Integer addressId) throws Exception {
        Optional<Address> addressOpt = addressRepository.findById(addressId);
        if (addressOpt.isPresent()) {
            return AddressConverter.convertToDto(addressOpt.get());
        } else {
            throw new Exception("Address not found with ID: " + addressId);
        }
    }

    @Override
    public List<AddressDto> getAllAddress(Integer userId) throws Exception {
        List<Address> addresses = addressRepository.findByUserId(userId);
        if (!addresses.isEmpty()) {
            return addresses.stream()
                    .map(AddressConverter::convertToDto)
                    .collect(Collectors.toList());
        } else {
            throw new Exception("No addresses found for user with ID: " + userId);
        }
    }

    @Override
    public void deleteAddress(Integer addressId) throws Exception {
        if (addressRepository.existsById(addressId)) {
            addressRepository.deleteById(addressId);
        } else {
            throw new Exception("Address not found with ID: " + addressId);
        }
    }

    @Override
    public AddressDto setDefaultAddress(Integer addressId, boolean status) throws Exception {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new Exception("Address not found"));

        Integer userId = address.getUser().getId();

        if (status) {
            // Nếu đặt địa chỉ này là mặc định (status = true),
            // thì cần đặt tất cả các địa chỉ khác của user thành false
            List<Address> userAddresses = addressRepository.findByUserId(userId);
            for (Address userAddress : userAddresses) {
                if (!userAddress.getAddressId().equals(addressId) && userAddress.isStatus()) {
                    userAddress.setStatus(false);
                    addressRepository.save(userAddress);
                }
            }
        }

        address.setStatus(status);

        Address updatedAddress = addressRepository.save(address);

        return AddressConverter.convertToDto(updatedAddress);
    }


}
