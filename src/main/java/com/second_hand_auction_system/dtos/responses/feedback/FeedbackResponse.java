package com.second_hand_auction_system.dtos.responses.feedback;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FeedbackResponse {

    private Integer feedbackId;
    private String comment;
    private int rating;
    private Integer userId;
    private String username;

    private String replyComment;
    private boolean replied;
    private Integer order;

    private Integer itemId;
    private String itemName;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;


}
