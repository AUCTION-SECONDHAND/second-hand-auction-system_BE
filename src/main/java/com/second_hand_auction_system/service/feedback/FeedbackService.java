package com.second_hand_auction_system.service.feedback;

import com.second_hand_auction_system.converters.feedback.FeedbackConverter;
import com.second_hand_auction_system.dtos.request.feedback.FeedbackDto;
import com.second_hand_auction_system.dtos.responses.feedback.FeedbackResponse;
import com.second_hand_auction_system.models.FeedBack;
import com.second_hand_auction_system.models.Item;
import com.second_hand_auction_system.models.User;
import com.second_hand_auction_system.repositories.FeedbackRepository;
import com.second_hand_auction_system.repositories.ItemRepository;
import com.second_hand_auction_system.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public FeedbackResponse createFeedback(FeedbackDto feedbackDto) throws Exception {
        User user = userRepository.findById(feedbackDto.getUserId())
                .orElseThrow(() -> new Exception("User not found"));
        Item item = itemRepository.findById(feedbackDto.getItemId())
                .orElseThrow(() -> new Exception("Item not found"));

        FeedBack feedback = FeedbackConverter.convertToEntity(feedbackDto, user, item);
        FeedBack savedFeedback = feedbackRepository.save(feedback);

        return FeedbackConverter.convertToResponse(savedFeedback);
    }

    @Override
    public FeedbackResponse updateFeedback(Integer feedbackId, FeedbackDto feedbackDto) throws Exception {
        FeedBack existingFeedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new Exception("Feedback not found"));

        existingFeedback.setComment(feedbackDto.getComment());
        existingFeedback.setRating(feedbackDto.getRating());

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
    public FeedbackResponse getFeedbackById(Integer feedbackId) throws Exception {
        FeedBack feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new Exception("Feedback not found"));
        return FeedbackConverter.convertToResponse(feedback);
    }

    @Override
    public List<FeedbackResponse> getAllFeedbacksSellerId(Integer itemId) throws Exception {
        List<FeedBack> feedbacks = feedbackRepository.findAllByItem_ItemId(itemId);
        return feedbacks.stream()
                .map(FeedbackConverter::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<FeedbackResponse> getAllFeedbacksByUserId(Integer userId) throws Exception {
        List<FeedBack> feedbacks = feedbackRepository.findAllByUser_Id(userId);
        return feedbacks.stream()
                .map(FeedbackConverter::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<FeedbackResponse> getFeedbackBySellerUserId(Integer userId, int page, int size) throws Exception {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createAt"))); // Sắp xếp theo createAt từ mới nhất đến cũ nhất
        Page<FeedBack> feedbackPage = feedbackRepository.findAllBySellerUserId(userId, pageable); // Sử dụng phương thức tìm kiếm phân trang trong repository

        // Chuyển đổi từ Page<FeedBack> sang Page<FeedbackResponse>
        return feedbackPage.map(FeedbackConverter::convertToResponse);
    }



}
