package com.second_hand_auction_system.converters.feedback;

import com.second_hand_auction_system.dtos.request.feedback.FeedbackDto;
import com.second_hand_auction_system.dtos.responses.feedback.FeedbackResponse;
import com.second_hand_auction_system.models.FeedBack;
import com.second_hand_auction_system.models.Item;
import com.second_hand_auction_system.models.Order;
import com.second_hand_auction_system.models.User;

public class FeedbackConverter {

    // Converter FeedbackDto => FeedBack entity
    public static FeedBack convertToEntity(FeedbackDto feedbackDto, User user, Item item, Order order) {
        FeedBack feedback = new FeedBack();
        feedback.setComment(feedbackDto.getComment());
        feedback.setRating(feedbackDto.getRating());
        feedback.setUser(user);
        feedback.setItem(item);
        feedback.setOrder(order);
        feedback.setReplyComment(null);
        feedback.setReplied(false);
        return feedback;
    }

    // Converter FeedBack entity => FeedbackResponse
    public static FeedbackResponse convertToResponse(FeedBack feedback) {
        return FeedbackResponse.builder()
                .feedbackId(feedback.getFeedbackId())
                .comment(feedback.getComment())
                .rating(feedback.getRating())
                .userId(feedback.getUser().getId())
                .username(feedback.getUser().getUsername())
                .itemId(feedback.getItem().getItemId())
                .itemName(feedback.getItem().getItemName())
                .order(feedback.getOrder() != null ? feedback.getOrder().getOrderId() : null)
                .replyComment(feedback.getReplyComment())
                .replied(feedback.isReplied())
                .createAt(feedback.getCreateAt())
                .updateAt(feedback.getUpdateAt())
                .build();
    }

    public static FeedbackResponse convertUpdatedToResponse(FeedBack feedback) {
        return FeedbackResponse.builder()
                .feedbackId(feedback.getFeedbackId())
                .comment(feedback.getComment())
                .rating(feedback.getRating())
                .userId(feedback.getUser().getId())
                .username(feedback.getUser().getUsername())
                .itemId(feedback.getItem().getItemId())
                .itemName(feedback.getItem().getItemName())
                .order(feedback.getOrder() != null ? feedback.getOrder().getOrderId() : null)
                .replyComment(feedback.getReplyComment())
                .replied(feedback.isReplied())
                .createAt(feedback.getCreateAt())
                .updateAt(feedback.getUpdateAt())
                .build();
    }


}
