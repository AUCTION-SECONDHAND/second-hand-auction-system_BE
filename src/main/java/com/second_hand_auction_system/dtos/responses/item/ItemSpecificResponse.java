package com.second_hand_auction_system.dtos.responses.item;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.*;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ItemSpecificResponse {
    @JsonProperty("item_spec_Id")
    private Integer itemSpecificationId;

    @JsonProperty("cpu")
    private String cpu;

    @JsonProperty("ram")
    private String ram;

    @JsonProperty("screen_size")
    private String screenSize;

    @JsonProperty("camera_specs")
    private String cameraSpecs;

    @JsonProperty("connectivity")
    private String connectivity;

    @JsonProperty("sensors")
    private String sensors;

}
