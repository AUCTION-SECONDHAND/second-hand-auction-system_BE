package com.second_hand_auction_system.dtos.responses.bid;

import lombok.*;

import java.sql.Time;
import java.time.LocalDateTime;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Builder
@Data
public class BidDetailResponse {
    private String thumbnail;
    private String itemDescription;
    private String itemName;
    private Double priceCurrent;
    private Integer itemId;
    private int numberOfBider;
    private int numberOfBid;
    private Time startTime;
    private Time endTime;
    private Date startDate;
    private Date endDate;
}
