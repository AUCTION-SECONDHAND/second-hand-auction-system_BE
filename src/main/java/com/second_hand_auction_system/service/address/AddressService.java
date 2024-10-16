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
        // Kiểm tra xem địa chỉ đầu tiên của người dùng có tồn tại không
        boolean isFirstAddress = addressRepository.findByUserId(addressDto.getUserId()).isEmpty();

        // Tạo một đối tượng User và thiết lập ID từ addressDto
        User user = new User();
        user.setId(addressDto.getUserId());

        Address address = AddressConverter.convertToEntity(addressDto, user);

        // Kiểm tra để thiết lập status
        address.setStatus(isFirstAddress);

        // Lưu địa chỉ vào repository
        Address savedAddress = addressRepository.save(address);

        // Chuyển đổi Address đã lưu thành AddressDto và trả về
        return AddressConverter.convertToDto(savedAddress);
    }




    @Override
    public AddressDto updateAddress(Integer addressId, AddressDto addressDto) throws Exception {
        Optional<Address> existingAddressOpt = addressRepository.findById(addressId);
        if (existingAddressOpt.isPresent()) {
            Address existingAddress = existingAddressOpt.get();

            User user = userRepository.findById(addressDto.getUserId())
                    .orElseThrow(() -> new Exception("User not found with ID: " + addressDto.getUserId()));


            Address updatedAddress = AddressConverter.convertToEntity(addressDto, user);
            updatedAddress.setAddressId(existingAddress.getAddressId());

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

        address.setStatus(status);

        Address setDefaultAddress = addressRepository.save(address);

        return AddressConverter.convertToDto(setDefaultAddress);
    }

}
