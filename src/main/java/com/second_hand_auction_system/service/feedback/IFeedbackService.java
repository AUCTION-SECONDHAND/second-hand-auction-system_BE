package com.second_hand_auction_system.service.feedback;

import com.second_hand_auction_system.dtos.request.feedback.FeedbackDto;
import com.second_hand_auction_system.dtos.responses.feedback.FeedbackResponses;

import java.util.List;

public interface IFeedbackService {
    FeedbackResponses createFeedback(FeedbackDto feedbackDto) throws Exception;

    FeedbackResponses updateFeedback(Integer feedbackId, FeedbackDto feedbackDto) throws Exception;

    void deleteFeedback(Integer feedbackId) throws Exception;

    FeedbackResponses getFeedbackById(Integer feedbackId) throws Exception;

    List<FeedbackResponses> getAllFeedbacksSellerId(Integer itemId) throws Exception;

    List<FeedbackResponses> getAllFeedbacksByUserId(Integer userId) throws Exception;
}
