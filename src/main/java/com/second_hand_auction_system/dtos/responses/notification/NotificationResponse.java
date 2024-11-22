package com.second_hand_auction_system.dtos.responses.notification;

import com.second_hand_auction_system.dtos.responses.BaseResponse;
import com.second_hand_auction_system.models.User;
import com.second_hand_auction_system.utils.NotificationStatus;
import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class NotificationResponse extends BaseResponse {

    private Integer notificationId;

    private String title;

    private String message;

    @Enumerated(EnumType.STRING)
    private NotificationStatus notificationStatus;

    private Integer user;

    private Boolean status;
}
