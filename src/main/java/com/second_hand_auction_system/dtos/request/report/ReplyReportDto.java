package com.second_hand_auction_system.dtos.request.report;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.second_hand_auction_system.utils.ReportStatus;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ReplyReportDto {

    private String responseMessage;

//    @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
//    private LocalDateTime responseCreateTime;
//
//    @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
//    private LocalDateTime responseUpdateTime;

    @Enumerated(EnumType.STRING)
    private ReportStatus status;
}
