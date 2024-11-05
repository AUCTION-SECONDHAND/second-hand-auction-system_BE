package com.second_hand_auction_system.dtos.responses.sellerInformation;

import com.second_hand_auction_system.dtos.responses.feedback.FeedbackResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class SellerInformationResponse {
    private Integer sellerId;
    private String storeName;
    private String address;
    private String description;
    private String avatar;
    private String backgroundImage;
    private Integer userId;
    private Integer totalFeedbackCount;
    private Double totalStars;
    private List<FeedbackResponse> feedbackList;
}