package com.second_hand_auction_system.dtos.responses.auction;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.second_hand_auction_system.utils.AuctionStatus;
import jakarta.persistence.Column;
import lombok.*;

import java.sql.Time;
import java.time.LocalDateTime;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ItemAuctionResponse {

    @JsonProperty("auction_id")
    private Integer auctionId;

    @JsonProperty("start_time")
    private Time startTime;

    @JsonProperty("end_time")
    private Time endTime;

    @JsonProperty("start_price")
    private double startPrice;

    @JsonProperty("approved_at")
    @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
    private Date approveAt;

    @JsonProperty("created_by")
    private String createBy;

    @Column(name = "start_date")
    @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
    private Date startDate;

    @Column(name = "end_date")
    @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
    private Date endDate;

    @Column(name = "status")
    private AuctionStatus status;

    @Column(name = "create_at")
    @JsonProperty("create_at")
    private LocalDateTime createAt;

    @Column(name = "update_at")
    @JsonProperty("update_at")
    private LocalDateTime updateAt;

}
