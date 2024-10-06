package com.second_hand_auction_system.service.subCategory;

import com.second_hand_auction_system.dtos.request.subCategory.SubCategoryDto;
import com.second_hand_auction_system.dtos.responses.subCategory.SubCategoryResponse;
import com.second_hand_auction_system.models.MainCategory;
import com.second_hand_auction_system.models.SubCategory;
import com.second_hand_auction_system.repositories.MainCategoryRepository;
import com.second_hand_auction_system.repositories.SubCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubCategoryService implements ISubCategoryService {
    private final SubCategoryRepository subCategoryRepository;
    private final MainCategoryRepository mainCategoryRepository;
    private final ModelMapper modelMapper;

    @Override
    public void addSubCategory(SubCategoryDto subCategory) throws Exception {

        MainCategory mainCategory = mainCategoryRepository.findById(subCategory.getMcId())
                .orElseThrow(() -> new Exception("Main Category not found"));
        SubCategory getSubCategoryName = subCategoryRepository.findBySubCategory(subCategory.getSubCategory());
        if (getSubCategoryName != null) {
            throw new Exception("SubCategory with name '" + subCategory.getSubCategory() + "' already exists");
        }
        SubCategory subCategoryEntity = modelMapper.map(subCategory, SubCategory.class);
        subCategoryEntity.setMainCategory(mainCategory);
        subCategoryRepository.save(subCategoryEntity);
    }

    @Override
    public void updateSubCategory(int scId, SubCategoryDto subCategory) throws Exception {
        MainCategory mainCategory = mainCategoryRepository.findById(subCategory.getMcId())
                .orElseThrow(() -> new Exception("Main Category not found"));
        SubCategory getSubCategoryName = subCategoryRepository.findBySubCategory(subCategory.getSubCategory());
        if (getSubCategoryName != null) {
            throw new Exception("SubCategory with name '" + subCategory.getSubCategory() + "' already exists");
        }
        SubCategory subCategoryEntity = subCategoryRepository.findById(scId)
                .orElseThrow(() -> new Exception("SubCategory not found"));
        modelMapper.map(subCategory, subCategoryEntity);
        subCategoryEntity.setMainCategory(mainCategory);
        subCategoryRepository.save(subCategoryEntity);
    }

    @Override
    public void deleteSubCategory(int scId) throws Exception {
        SubCategory subCategoryEntity = subCategoryRepository.findById(scId)
                .orElseThrow(() -> new Exception("SubCategory not found"));
        subCategoryRepository.delete(subCategoryEntity);

    }

    @Override
    public List<SubCategoryResponse> getSubCategory() throws Exception {
        List<SubCategory> subCategoryEntities = subCategoryRepository.findAll();
        List<SubCategoryResponse> subCategoryResponses = subCategoryEntities.stream()
                .map(subCategory -> modelMapper.map(subCategory, SubCategoryResponse.class))
                .toList();
        return subCategoryResponses;
    }
}
