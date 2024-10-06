package com.second_hand_auction_system.controller;

import com.second_hand_auction_system.dtos.request.subCategory.SubCategoryDto;
import com.second_hand_auction_system.dtos.responses.ResponseObject;
import com.second_hand_auction_system.dtos.responses.subCategory.SubCategoryResponse;
import com.second_hand_auction_system.service.subCategory.ISubCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/sub-category")
public class SubCategoryController {
    private final ISubCategoryService subCategoryService;

    @PostMapping("")
    public ResponseEntity<?> createSubCategory(
            @Valid @RequestBody SubCategoryDto subCategoryDto,
            BindingResult result
    ) throws Exception {
        if (result.hasErrors()) {
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(
                    ResponseObject.builder()
                            .status(HttpStatus.BAD_REQUEST)
                            .message(String.valueOf(errorMessages))
                            .build()
            );
        }
        subCategoryService.addSubCategory(subCategoryDto);
        return ResponseEntity.ok(
                ResponseObject.builder()
                        .status(HttpStatus.OK)
                        .message("Success")
                        .build()
        );
    }

    @PutMapping("/{scId}")
    public ResponseEntity<?> updateSubCategory(
            @PathVariable int scId,
            @Valid @RequestBody SubCategoryDto subCategoryDto,
            BindingResult result
    ) throws Exception {
        if (result.hasErrors()) {
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(
                    ResponseObject.builder()
                            .status(HttpStatus.BAD_REQUEST)
                            .message(String.valueOf(errorMessages))
                            .build()
            );
        }
        subCategoryService.updateSubCategory(scId, subCategoryDto);
        return ResponseEntity.ok(
                ResponseObject.builder()
                        .status(HttpStatus.OK)
                        .message("Success")
                        .build()
        );
    }

    @DeleteMapping("{scId}")
    public ResponseEntity<?> deleteSubCategory(
            @PathVariable int scId
    ) throws Exception {
        subCategoryService.deleteSubCategory(scId);
        return ResponseEntity.ok(
                ResponseObject.builder()
                        .status(HttpStatus.OK)
                        .message("Success")
                        .build()
        );
    }

    @GetMapping("")
    public ResponseEntity<?> findAllSubCategories() throws Exception {
        List<SubCategoryResponse> subCategoryResponses = subCategoryService.getSubCategory();
        return ResponseEntity.ok(
                ResponseObject.builder()
                        .status(HttpStatus.OK)
                        .message("Success")
                        .data(subCategoryResponses)
                        .build()
        );
    }
}
