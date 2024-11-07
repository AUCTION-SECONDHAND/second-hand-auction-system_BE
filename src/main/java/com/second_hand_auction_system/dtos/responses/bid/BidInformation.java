package com.second_hand_auction_system.dtos.responses.bid;

import lombok.*;

import java.sql.Time;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BidInformation {
    private int qualityBid;
    private Double minimumBidPrice1;
    private Double minimumBidPrice2;
    private Double minimumBidPrice3;
    private Time startTime;
    private Time endTime;
    private Date startDate;
    private Date endDate;
    private double priceStep;
}
