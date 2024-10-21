package com.second_hand_auction_system.converters.feedback;

import com.second_hand_auction_system.dtos.request.feedback.FeedbackDto;
import com.second_hand_auction_system.dtos.responses.feedback.FeedbackResponses;
import com.second_hand_auction_system.models.FeedBack;
import com.second_hand_auction_system.models.Item;
import com.second_hand_auction_system.models.User;

public class FeedbackConverter {

    // Converter FeedbackDto => FeedBack entity
    public static FeedBack convertToEntity(FeedbackDto feedbackDto, User user, Item item) {
        FeedBack feedback = new FeedBack();
        feedback.setComment(feedbackDto.getComment());
        feedback.setRating(feedbackDto.getRating());
        feedback.setImageUrl(feedbackDto.getImageUrl());
        feedback.setUser(user);
        feedback.setItem(item);
        return feedback;
    }

    // Converter FeedBack entity => FeedbackResponses
    public static FeedbackResponses convertToResponse(FeedBack feedback) {
        return FeedbackResponses.builder()
                .feedbackId(feedback.getFeedbackId())
                .comment(feedback.getComment())
                .rating(feedback.getRating())
                .imageUrl(feedback.getImageUrl())
                .userId(feedback.getUser().getId())
                .username(feedback.getUser().getUsername())
                .itemId(feedback.getItem().getItemId())
                .itemName(feedback.getItem().getItemName())
                .build();
    }
}
