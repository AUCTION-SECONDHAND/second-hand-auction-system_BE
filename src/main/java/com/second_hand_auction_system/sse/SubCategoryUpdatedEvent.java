package com.second_hand_auction_system.sse;

import com.second_hand_auction_system.dtos.responses.subCategory.SubCategoryResponse;

import java.util.List;

public class SubCategoryUpdatedEvent {
    private final List<SubCategoryResponse> updatedSubCategories;

    public SubCategoryUpdatedEvent(List<SubCategoryResponse> updatedSubCategories) {
        this.updatedSubCategories = updatedSubCategories;
    }

    public List<SubCategoryResponse> getUpdatedSubCategories() {
        return updatedSubCategories;
    }
}
