package com.second_hand_auction_system.dtos.responses.sellerInformation;

import com.second_hand_auction_system.dtos.responses.feedback.FeedbackResponse;
import lombok.*;

import java.time.LocalDateTime;
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

    private double rating1Percentage;
    private double rating2Percentage;
    private double rating3Percentage;
    private double rating4Percentage;
    private double rating5Percentage;

    private LocalDateTime sellerCreateAt;

}