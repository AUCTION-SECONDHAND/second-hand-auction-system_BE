package com.second_hand_auction_system.converters.feedback;

import com.second_hand_auction_system.dtos.request.feedback.FeedbackDto;
import com.second_hand_auction_system.dtos.responses.feedback.FeedbackResponse;
import com.second_hand_auction_system.models.FeedBack;
import com.second_hand_auction_system.models.Item;
import com.second_hand_auction_system.models.User;

public class FeedbackConverter {

    // Converter FeedbackDto => FeedBack entity
    public static FeedBack convertToEntity(FeedbackDto feedbackDto, User user, Item item) {
        FeedBack feedback = new FeedBack();
        feedback.setComment(feedbackDto.getComment());
        feedback.setRating(feedbackDto.getRating());
        feedback.setUser(user);
        feedback.setItem(item);
        return feedback;
    }

    // Converter FeedBack entity => FeedbackResponses
    public static FeedbackResponse convertToResponse(FeedBack feedback) {
        return FeedbackResponse.builder()
                .feedbackId(feedback.getFeedbackId())
                .comment(feedback.getComment())
                .rating(feedback.getRating())
                .userId(feedback.getUser().getId())
                .username(feedback.getUser().getUsername())
                .itemId(feedback.getItem().getItemId())
                .itemName(feedback.getItem().getItemName())
                .createAt(feedback.getCreateAt())
                .updateAt(feedback.getUpdateAt())
                .build();
    }
}
