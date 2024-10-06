package com.second_hand_auction_system.dtos.request.subCategory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.second_hand_auction_system.models.MainCategory;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubCategoryDto {

    //private int subCategoryId;
    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 200, message = "SubCategory must be between 3 and 200 characters")
    @JsonProperty("sub_category")
    private String subCategory;

    private String description;
    //main-categoryId
    @JsonProperty("mc_id")
    private Integer mcId;

//    @JsonProperty("sub_category_id")
//    private Integer mainCategory;
}
