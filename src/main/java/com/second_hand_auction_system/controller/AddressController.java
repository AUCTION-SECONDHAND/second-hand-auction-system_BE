package com.second_hand_auction_system.controller;

import com.second_hand_auction_system.dtos.request.address.AddressDto;
import com.second_hand_auction_system.dtos.responses.address.AddressResponse;
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
    public ResponseEntity<AddressResponse> createAddress(@RequestBody AddressDto addressDto) {
        try {
            AddressResponse createdAddress = addressService.createAddress(addressDto);
            return new ResponseEntity<>(createdAddress, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress(@PathVariable Integer addressId, @RequestBody AddressDto addressDto) {
        try {
            AddressResponse updatedAddress = addressService.updateAddress(addressId, addressDto);
            return new ResponseEntity<>(updatedAddress, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/{addressId}/status")
    public ResponseEntity<AddressResponse> setDefaultAddress(@PathVariable Integer addressId) {
        try {
            AddressResponse updatedAddress = addressService.setDefaultAddress(addressId);
            return new ResponseEntity<>(updatedAddress, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/{addressId}")
    public ResponseEntity<AddressResponse> getAddressById(@PathVariable Integer addressId) {
        try {
            AddressResponse address = addressService.getAddressById(addressId);
            return new ResponseEntity<>(address, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/user")
    public ResponseEntity<List<AddressResponse>> getAllAddress() {
        try {
            List<AddressResponse> addresses = addressService.getAllAddress();
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

    @GetMapping("/address-order")
    public ResponseEntity<AddressResponse> getAllAddressOrder() {
        return addressService.getAddressOrder();
    }
}
