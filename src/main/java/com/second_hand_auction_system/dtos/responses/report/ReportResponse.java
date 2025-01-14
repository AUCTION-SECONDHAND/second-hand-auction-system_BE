package com.second_hand_auction_system.dtos.responses.report;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class ReportResponse {
    private Integer reportId;

    private String evidence;

    private String reason;

    private String createBy;

    @Enumerated(EnumType.STRING)
    private ReportPriority priority;

    @Enumerated(EnumType.STRING)
    private ReportType type;

    @Enumerated(EnumType.STRING)
    private ReportStatus status;

    private String processedBy;

    private String responseMessage;

    @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
    private LocalDateTime responseCreateTime;

    @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
    private LocalDateTime responseUpdateTime;
    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", shape = JsonFormat.Shape.STRING)
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", shape = JsonFormat.Shape.STRING)
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    private Integer orderId;

    private String orderCode;

    private String ticketId;

}
