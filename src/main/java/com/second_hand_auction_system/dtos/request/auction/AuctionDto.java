package com.second_hand_auction_system.dtos.request.auction;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.second_hand_auction_system.models.Item;
import com.second_hand_auction_system.utils.AuctionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.sql.Time;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuctionDto {

    @JsonProperty("start_time")
    @NotNull(message = "Start time is required")
    @JsonFormat(pattern = "HH:mm:ss", shape = JsonFormat.Shape.STRING)
    private Time startTime;

    @JsonProperty("end_time")
    @NotNull(message = "End time is required")
    @JsonFormat(pattern = "HH:mm:ss", shape = JsonFormat.Shape.STRING)
    private Time endTime;

    @JsonProperty("start_price")
    @Positive(message = "Start price must be a positive value")
    private double startPrice;

    @JsonProperty("description")
    @Size(max = 500, message = "Description should not exceed 500 characters")
    private String description;

    @JsonProperty("terms_conditions")
    @Size(max = 1000, message = "Terms and conditions should not exceed 1000 characters")
    private String termConditions;

    @JsonProperty("price_step")
    @Positive(message = "Price step must be a positive value")
    private double priceStep;

    @JsonProperty("ship_type")
    @Size(max = 100, message = "Ship type should not exceed 100 characters")
    private String shipType;

    @JsonProperty("comment")
    @Size(max = 300, message = "Comment should not exceed 300 characters")
    private String comment;

    @JsonProperty("status")
    @NotNull(message = "Status is required")
    private AuctionStatus status;

    @JsonProperty("approved_by")
    private String approveBy;

    @JsonProperty("approved_at")
    private Date approveAt;

    @JsonProperty("created_by")
    private String createBy;

    @JsonProperty("item")
    @NotNull(message = "Item is required")
    private Integer item;

    @JsonProperty("end_date")
    @NotNull(message = "End date is required")
    @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
    private Date endDate;

    @JsonProperty("start_date")
    @NotNull(message = "Start date is required")
    @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
    private Date startDate;
}
