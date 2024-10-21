package com.second_hand_auction_system.service.sellerInformation;

import com.second_hand_auction_system.converters.sellerInformation.SellerInformationConverter;
import com.second_hand_auction_system.dtos.request.sellerInfomation.SellerInformationDto;
import com.second_hand_auction_system.dtos.responses.sellerInformation.SellerInformationResponse;
import com.second_hand_auction_system.models.SellerInformation;
import com.second_hand_auction_system.models.User;
import com.second_hand_auction_system.repositories.SellerInformationRepository;
import com.second_hand_auction_system.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class SellerInformationService implements ISellerInformationService {

    private final SellerInformationRepository sellerInformationRepository;
    private final UserRepository userRepository;

    @Override
    public SellerInformationResponse createSellerInformation(SellerInformationDto sellerInformationDto, Integer userId) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + userId));

        SellerInformation sellerInformation = SellerInformationConverter.convertToEntity(sellerInformationDto, user);

        SellerInformation savedSellerInfo = sellerInformationRepository.save(sellerInformation);

        return SellerInformationConverter.convertToResponse(savedSellerInfo);
    }

    @Override
    public SellerInformationResponse updateSellerInformation(Integer sellerId, SellerInformationDto sellerInformationDto) throws Exception {
        SellerInformation existingSellerInfo = sellerInformationRepository.findById(sellerId)
                .orElseThrow(() -> new NoSuchElementException("Seller information not found with ID: " + sellerId));

        existingSellerInfo.setStoreName(sellerInformationDto.getStoreName());
        existingSellerInfo.setAddress(sellerInformationDto.getAddress());
        existingSellerInfo.setDescription(sellerInformationDto.getDescription());
        existingSellerInfo.setAvatar(sellerInformationDto.getAvatar());
        existingSellerInfo.setBackgroundImage(sellerInformationDto.getBackgroundImage());

        SellerInformation updatedSellerInfo = sellerInformationRepository.save(existingSellerInfo);

        return SellerInformationConverter.convertToResponse(updatedSellerInfo);
    }

    @Override
    public SellerInformationResponse getSellerInformationById(Integer sellerId) throws Exception {
        SellerInformation sellerInformation = sellerInformationRepository.findById(sellerId)
                .orElseThrow(() -> new NoSuchElementException("Seller information not found with ID: " + sellerId));

        return SellerInformationConverter.convertToResponse(sellerInformation);
    }

    @Override
    public void deleteSellerInformation(Integer sellerId) throws Exception {
        SellerInformation sellerInformation = sellerInformationRepository.findById(sellerId)
                .orElseThrow(() -> new NoSuchElementException("Seller information not found with ID: " + sellerId));

        sellerInformationRepository.delete(sellerInformation);
    }
}
