package com.second_hand_auction_system.dtos.responses.mainCategory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.second_hand_auction_system.dtos.responses.BaseResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MainCategoryResponse extends BaseResponse {
    @JsonProperty("main_category_id")
    private Integer mainCategoryId;

    @JsonProperty("category_name")
    private String categoryName;

    @JsonProperty("description")
    private String description;

    @JsonProperty("icon_url")
    private String iconUrl;

}
