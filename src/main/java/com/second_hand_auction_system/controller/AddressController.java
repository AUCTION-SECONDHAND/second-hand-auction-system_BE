package com.second_hand_auction_system.controller;

import com.second_hand_auction_system.dtos.request.address.AddressDto;
import com.second_hand_auction_system.service.address.IAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/address")
public class AddressController {
    private final IAddressService addressService;

    @PostMapping
    public ResponseEntity<AddressDto> createAddress(@RequestBody AddressDto addressDto) {
        try {
            AddressDto createdAddress = addressService.createAddress(addressDto);
            return new ResponseEntity<>(createdAddress, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<AddressDto> updateAddress(@PathVariable Integer addressId, @RequestBody AddressDto addressDto) {
        try {
            AddressDto updatedAddress = addressService.updateAddress(addressId, addressDto);
            return new ResponseEntity<>(updatedAddress, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/{addressId}/status")
    public ResponseEntity<AddressDto> updateAddressStatus(
            @PathVariable Integer addressId,
            @RequestBody boolean status) {
        try {
            AddressDto updatedAddress = addressService.setDefaultAddress(addressId, status);
            return new ResponseEntity<>(updatedAddress, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{addressId}")
    public ResponseEntity<AddressDto> getAddressById(@PathVariable Integer addressId) {
        try {
            AddressDto address = addressService.getAddressById(addressId);
            return new ResponseEntity<>(address, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AddressDto>> getAllAddress(@PathVariable Integer userId) {
        try {
            List<AddressDto> addresses = addressService.getAllAddress(userId);
            return new ResponseEntity<>(addresses, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Integer addressId) {
        try {
            addressService.deleteAddress(addressId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
