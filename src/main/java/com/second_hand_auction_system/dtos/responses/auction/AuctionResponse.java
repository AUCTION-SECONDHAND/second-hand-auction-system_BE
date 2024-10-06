package com.second_hand_auction_system.dtos.responses.auction;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.second_hand_auction_system.dtos.responses.BaseResponse;
import com.second_hand_auction_system.utils.AuctionStatus;

import java.time.LocalDateTime;
import java.util.Date;

public class AuctionResponse extends BaseResponse {

    @JsonProperty("start_time")
    private LocalDateTime startTime;

    @JsonProperty("end_time")
    private LocalDateTime endTime;

    @JsonProperty("start_price")
    private double startPrice;

    @JsonProperty("description")
    private String description;

    @JsonProperty("terms_conditions")
    private String termConditions;

    @JsonProperty("price_step")
    private double priceStep;

    @JsonProperty("ship_type")
    private String shipType;

    @JsonProperty("comment")
    private String comment;

    @JsonProperty("status")
    private AuctionStatus status;

    @JsonProperty("approved_by")
    private String approveBy;

    @JsonProperty("approved_at")
    private Date approveAt;

    @JsonProperty("created_by")
    private String createBy;

    @JsonProperty("item")
    private Integer item;
}
