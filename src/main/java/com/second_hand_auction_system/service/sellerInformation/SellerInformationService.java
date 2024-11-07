package com.second_hand_auction_system.service.sellerInformation;

import com.second_hand_auction_system.converters.sellerInformation.SellerInformationConverter;
import com.second_hand_auction_system.dtos.request.sellerInfomation.SellerInformationDto;
import com.second_hand_auction_system.dtos.responses.feedback.FeedbackResponse;
import com.second_hand_auction_system.dtos.responses.sellerInformation.SellerInformationResponse;
import com.second_hand_auction_system.models.Item;
import com.second_hand_auction_system.models.SellerInformation;
import com.second_hand_auction_system.models.User;
import com.second_hand_auction_system.repositories.ItemRepository;
import com.second_hand_auction_system.repositories.SellerInformationRepository;
import com.second_hand_auction_system.repositories.UserRepository;
import com.second_hand_auction_system.service.feedback.IFeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class SellerInformationService implements ISellerInformationService {

    private final SellerInformationRepository sellerInformationRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final IFeedbackService feedbackService;

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

    @Override
    public SellerInformationResponse getSellerInformationByUserId(Integer userId) throws Exception {
        SellerInformation sellerInformation = sellerInformationRepository.findByUser_Id(userId)
                .orElseThrow(() -> new NoSuchElementException("Seller information not found for user ID: " + userId));

        return SellerInformationConverter.convertToResponse(sellerInformation);
    }

    @Override
    public SellerInformationResponse getSellerInformationByAuctionId(Integer auctionId) throws Exception {
        Item item = itemRepository.findByAuction_AuctionId(auctionId);
        if (item == null) {
            throw new NoSuchElementException("Item not found for auctionId: " + auctionId);
        }

        SellerInformation sellerInformation = sellerInformationRepository.findByUser_Id(item.getUser().getId())
                .orElseThrow(() -> new NoSuchElementException("Seller information not found for user ID: " + item.getUser().getId()));

        Page<FeedbackResponse> feedbackResponses = feedbackService.getFeedbackBySellerUserId(item.getUser().getId(), 0, 10);

        return SellerInformationConverter.convertToResponseWithFeedback(sellerInformation, feedbackResponses);
    }



}
