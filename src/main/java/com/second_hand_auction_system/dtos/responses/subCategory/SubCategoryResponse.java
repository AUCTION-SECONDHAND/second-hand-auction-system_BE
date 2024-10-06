package com.second_hand_auction_system.dtos.responses.subCategory;

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
public class SubCategoryResponse extends BaseResponse {

    @JsonProperty("sc_id")
    private int subCategoryId;

    @JsonProperty("sub_category")
    private String subCategory;

    private String description;

    //main-categoryId
//    @JsonProperty("mc_id")
//    private Integer mcId;

}
