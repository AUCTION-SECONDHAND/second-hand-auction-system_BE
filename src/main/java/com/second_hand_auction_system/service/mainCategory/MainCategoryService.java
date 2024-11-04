package com.second_hand_auction_system.service.mainCategory;

import com.second_hand_auction_system.converters.mainCategory.MainCategoryConverter;
import com.second_hand_auction_system.dtos.request.mainCategory.MainCategoryDto;
import com.second_hand_auction_system.dtos.responses.ResponseObject;
import com.second_hand_auction_system.dtos.responses.mainCategory.CategoryVsSubCategoryResponse;
import com.second_hand_auction_system.dtos.responses.mainCategory.MainCategoryResponse;
import com.second_hand_auction_system.models.MainCategory;
import com.second_hand_auction_system.repositories.MainCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MainCategoryService implements IMainCategoryService {
    private final MainCategoryRepository mainCategoryRepository;
    private final ModelMapper modelMapper;
    private final MainCategoryConverter mainCategoryConverter;

    @Override
    public void addMainCategory(MainCategoryDto mainCategory) throws Exception {
        MainCategory mainCategoryExisted = mainCategoryRepository.findByCategoryName(mainCategory.getCategoryName());
        if (mainCategoryExisted != null) {
            throw new Exception("MainCategory with name '" + mainCategory.getCategoryName() + "' already exists");
        }
        MainCategory mainCategoryExist = modelMapper.map(mainCategory, MainCategory.class);
        mainCategoryRepository.save(mainCategoryExist);
    }

    @Override
    public void updateMainCategory(int id, MainCategoryDto mainCategory) throws Exception {
        MainCategory mainCategoryExist = mainCategoryRepository.findByCategoryName(mainCategory.getCategoryName());
        if (mainCategoryExist != null) {
            throw new Exception("MainCategory with name '" + mainCategory.getCategoryName() + "' already exists");
        }
        MainCategory mainCategoryIdExisted = mainCategoryRepository.findById(id)
                .orElseThrow(() -> new Exception("MainCategory not found"));
        modelMapper.map(mainCategory, mainCategoryIdExisted);
        mainCategoryRepository.save(mainCategoryIdExisted);
    }

    @Override
    public void deleteMainCategory(int id) throws Exception {
        MainCategory mainCategoryExisted = mainCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("MainCategory not found"));
        mainCategoryRepository.delete(mainCategoryExisted);
    }

    @Override
    public List<MainCategoryResponse> getMainCategory() throws Exception {
        List<MainCategory> mainCategoryList = mainCategoryRepository.findAll();
        List<MainCategoryResponse> mainCategoryResponses = mainCategoryList.stream()
                .map(mainCategory -> modelMapper.map(mainCategory, MainCategoryResponse.class))
                .collect(Collectors.toList());
        return mainCategoryResponses;
    }
    @Override
    public MainCategoryResponse getMainCategoryTest(int id) throws Exception {
        MainCategory mainCategory = mainCategoryRepository.findByMainCategoryId(id);
        if (mainCategory == null) {
            throw new Exception("MainCategory not found");
        }
        MainCategoryResponse mainCategoryResponse = modelMapper.map(mainCategory, MainCategoryResponse.class);
        return mainCategoryResponse;
    }

    @Override
    public List<CategoryVsSubCategoryResponse> getMainCategoryVsSubCategory() throws Exception {
        List<MainCategory> mainCategoryList = mainCategoryRepository.findAll();
        return mainCategoryList.stream().map(mainCategoryConverter::toCategoryVsSubCategoryResponse).collect(Collectors.toList());
    }

    @Override
    public ResponseEntity<?> getMainCategorys() {
        List<MainCategory> mainCategoryList = mainCategoryRepository.findAll();
        List<MainCategoryResponse> mainCategoryResponses = mainCategoryList.stream()
                .map(mainCategory -> modelMapper.map(mainCategory, MainCategoryResponse.class))
                .toList();
        return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder().status(HttpStatus.OK)
                .data(mainCategoryResponses)
                .message("List of main categories")
                .build());
    }

}
