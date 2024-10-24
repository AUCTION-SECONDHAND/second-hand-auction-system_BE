package com.second_hand_auction_system.dtos.responses.item;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ItemSpecificResponse {

    private double percent;

    private String type;

    private String color;

    private double weight;

    private String dimension;

    private String original;

    @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
    private Date manufactureDate;

    private String material;

}
