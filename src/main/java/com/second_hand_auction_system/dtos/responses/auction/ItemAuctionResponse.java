package com.second_hand_auction_system.dtos.responses.auction;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.second_hand_auction_system.utils.AuctionStatus;
import jakarta.persistence.Column;
import lombok.*;

import java.sql.Time;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ItemAuctionResponse {
    @JsonProperty("start_time")
    private Time startTime;

    @JsonProperty("end_time")
    private Time endTime;

    @JsonProperty("start_price")
    private double startPrice;

    @JsonProperty("approved_at")
    private Date approveAt;

    @JsonProperty("created_by")
    private String createBy;

    @Column(name = "start_date")
    private Date startDate;

    @Column(name = "end_date")
    private Date endDate;

    @Column(name = "status")
    private AuctionStatus status;


}
