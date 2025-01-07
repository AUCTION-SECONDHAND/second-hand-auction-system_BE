package com.second_hand_auction_system.dtos.request.report;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.second_hand_auction_system.models.User;
import com.second_hand_auction_system.utils.ReportPriority;
import com.second_hand_auction_system.utils.ReportStatus;
import com.second_hand_auction_system.utils.ReportType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ReportDto {

    //private String evidence;

    private String reason;

    //private ReportPriority priority;

    private ReportType type;

//    @Column(name = "response_message")
//    private String responseMessage;
}
