package com.second_hand_auction_system.service.subCategory;


import com.second_hand_auction_system.dtos.request.subCategory.SubCategoryDto;
import com.second_hand_auction_system.dtos.responses.subCategory.SubCategoryResponse;

import java.util.List;

public interface ISubCategoryService {
    void addSubCategory(SubCategoryDto subCategory) throws Exception;
    void updateSubCategory(int scId, SubCategoryDto subCategory) throws Exception;
    void deleteSubCategory(int scId) throws Exception;
    List<SubCategoryResponse> getSubCategory() throws Exception;
}
