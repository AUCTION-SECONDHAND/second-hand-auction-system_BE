package com.second_hand_auction_system.service.feedback;

import com.second_hand_auction_system.converters.feedback.FeedbackConverter;
import com.second_hand_auction_system.dtos.request.feedback.FeedbackDto;
import com.second_hand_auction_system.dtos.responses.feedback.FeedbackResponses;
import com.second_hand_auction_system.models.FeedBack;
import com.second_hand_auction_system.models.Item;
import com.second_hand_auction_system.models.User;
import com.second_hand_auction_system.repositories.FeedbackRepository;
import com.second_hand_auction_system.repositories.ItemRepository;
import com.second_hand_auction_system.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FeedbackService implements IFeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Override
    public FeedbackResponses createFeedback(FeedbackDto feedbackDto) throws Exception {
        User user = userRepository.findById(feedbackDto.getUserId())
                .orElseThrow(() -> new Exception("User not found"));
        Item item = itemRepository.findById(feedbackDto.getItemId())
                .orElseThrow(() -> new Exception("Item not found"));

        FeedBack feedback = FeedbackConverter.convertToEntity(feedbackDto, user, item);
        FeedBack savedFeedback = feedbackRepository.save(feedback);

        return FeedbackConverter.convertToResponse(savedFeedback);
    }

    @Override
    public FeedbackResponses updateFeedback(Integer feedbackId, FeedbackDto feedbackDto) throws Exception {
        FeedBack existingFeedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new Exception("Feedback not found"));

        existingFeedback.setComment(feedbackDto.getComment());
        existingFeedback.setRating(feedbackDto.getRating());
        existingFeedback.setImageUrl(feedbackDto.getImageUrl());

        FeedBack updatedFeedback = feedbackRepository.save(existingFeedback);
        return FeedbackConverter.convertToResponse(updatedFeedback);
    }

    @Override
    public void deleteFeedback(Integer feedbackId) throws Exception {
        if (!feedbackRepository.existsById(feedbackId)) {
            throw new Exception("Feedback not found");
        }
        feedbackRepository.deleteById(feedbackId);
    }

    @Override
    public FeedbackResponses getFeedbackById(Integer feedbackId) throws Exception {
        FeedBack feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new Exception("Feedback not found"));
        return FeedbackConverter.convertToResponse(feedback);
    }

    @Override
    public List<FeedbackResponses> getAllFeedbacksSellerId(Integer itemId) throws Exception {
        List<FeedBack> feedbacks = feedbackRepository.findAllByItem_ItemId(itemId);
        return feedbacks.stream()
                .map(FeedbackConverter::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<FeedbackResponses> getAllFeedbacksByUserId(Integer userId) throws Exception {
        List<FeedBack> feedbacks = feedbackRepository.findAllByUser_Id(userId);
        return feedbacks.stream()
                .map(FeedbackConverter::convertToResponse)
                .collect(Collectors.toList());
    }
}
