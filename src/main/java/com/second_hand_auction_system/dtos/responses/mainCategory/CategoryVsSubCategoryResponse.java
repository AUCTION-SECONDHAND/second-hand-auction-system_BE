package com.second_hand_auction_system.dtos.responses.mainCategory;

import com.second_hand_auction_system.dtos.responses.subCategory.SubCategoryResponse;
import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryVsSubCategoryResponse {
    private int categoryId;
    private String categoryName;
    private List<SubCategoryResponse> subCategory;
}
