package com.second_hand_auction_system.dtos.request.feedback;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.second_hand_auction_system.models.Order;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FeedbackDto {
    private Integer feedbackId;

    @NotBlank(message = "Comment is required")
    private String comment;

    @NotNull(message = "Rating is required")
    private int rating;

    private String replyComment;
    private boolean replied;
    private Integer orderId;


    @NotNull(message = "User ID is required")
    @JsonProperty
    private Integer userId;

    private Integer itemId;
}
