package com.second_hand_auction_system.dtos.responses.sellerInformation;

import com.second_hand_auction_system.dtos.responses.feedback.FeedbackResponse;
import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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