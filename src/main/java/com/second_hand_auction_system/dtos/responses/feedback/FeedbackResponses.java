package com.second_hand_auction_system.dtos.responses.feedback;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FeedbackResponses {

    private Integer feedbackId;
    private String comment;
    private int rating;
    private String imageUrl;
    private Integer userId;
    private String username;
    private Integer itemId;
    private String itemName;
}
