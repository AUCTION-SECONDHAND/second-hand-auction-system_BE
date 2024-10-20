package com.second_hand_auction_system.dtos.responses.subCategory;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class SubCategoryItemResponse {
    @JsonProperty("sub_category")
    private String subCategory;
}
