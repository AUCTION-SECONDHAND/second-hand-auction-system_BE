package com.second_hand_auction_system.converters.mainCategory;

import com.second_hand_auction_system.dtos.responses.mainCategory.CategoryVsSubCategoryResponse;
import com.second_hand_auction_system.dtos.responses.subCategory.SubCategoryResponse;
import com.second_hand_auction_system.models.MainCategory;
import com.second_hand_auction_system.models.SubCategory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MainCategoryConverter {
    public CategoryVsSubCategoryResponse toCategoryVsSubCategoryResponse(MainCategory mainCategory) {
        List<SubCategoryResponse> subCategoryResponses = mainCategory.getSubCategories().stream()
                .map(subCategory -> SubCategoryResponse.builder()
                        .subCategoryId(subCategory.getSubCategoryId())
                        .subCategory(subCategory.getSubCategory())
                        .description(subCategory.getDescription())
                        .build())
                .collect(Collectors.toList());

        return CategoryVsSubCategoryResponse.builder()
                .categoryId(mainCategory.getMainCategoryId())
                .categoryName(mainCategory.getCategoryName())
                .categoryImage(mainCategory.getIconUrl())
                .subCategory(subCategoryResponses)
                .build();
    }
}
