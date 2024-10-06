package com.second_hand_auction_system.service.mainCategory;

import com.second_hand_auction_system.dtos.request.mainCategory.MainCategoryDto;
import com.second_hand_auction_system.dtos.responses.mainCategory.MainCategoryResponse;

import java.util.List;

public interface IMainCategoryService {
    void addMainCategory(MainCategoryDto mainCategory) throws Exception;
    void updateMainCategory(int id, MainCategoryDto mainCategory) throws Exception;
    void deleteMainCategory(int id) throws Exception;
    List<MainCategoryResponse> getMainCategory() throws Exception;
    MainCategoryResponse getMainCategoryTest(int id) throws Exception;
}
