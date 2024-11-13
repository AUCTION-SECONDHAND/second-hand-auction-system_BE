package com.second_hand_auction_system.service.feedback;

import com.second_hand_auction_system.dtos.request.feedback.FeedbackDto;
import com.second_hand_auction_system.dtos.responses.feedback.FeedbackResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IFeedbackService {
    FeedbackResponse createFeedback(FeedbackDto feedbackDto) throws Exception;

    FeedbackResponse updateFeedback(Integer feedbackId, FeedbackDto feedbackDto) throws Exception;

    void deleteFeedback(Integer feedbackId) throws Exception;

    FeedbackResponse getFeedbackById(Integer feedbackId) throws Exception;

    List<FeedbackResponse> getAllFeedbacksSellerId(Integer itemId) throws Exception;

    List<FeedbackResponse> getAllFeedbacksByUserId(Integer userId) throws Exception;

    Page<FeedbackResponse> getFeedbackBySellerUserId(Integer userId, int page, int size) throws Exception;

    FeedbackResponse checkFeedbackExistsByOrderId(Integer orderId) throws Exception;

}
