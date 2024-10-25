package com.second_hand_auction_system.controller;

import com.second_hand_auction_system.dtos.request.item.ItemApprove;
import com.second_hand_auction_system.dtos.request.item.ItemDto;
import com.second_hand_auction_system.dtos.responses.ResponseListObject;
import com.second_hand_auction_system.dtos.responses.ResponseObject;
import com.second_hand_auction_system.dtos.responses.auctionRegistrations.AuctionRegistrationsResponse;
import com.second_hand_auction_system.dtos.responses.item.AuctionItemResponse;
import com.second_hand_auction_system.dtos.responses.item.ItemDetailResponse;
import com.second_hand_auction_system.service.item.IItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/item")
public class ItemController {
    private final IItemService itemService;

    @PostMapping("")
    public ResponseEntity<?> createItem(
            @Valid @RequestBody ItemDto itemDto,
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
        itemService.addItem(itemDto);
        return ResponseEntity.ok(
                ResponseObject.builder()
                        .status(HttpStatus.OK)
                        .message("Success")
                        .build()
        );
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<?> updateItem(
            @PathVariable Integer itemId,
            @Valid @RequestBody ItemDto itemDto,
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
        itemService.updateItem(itemId, itemDto);
        return ResponseEntity.ok(
                ResponseObject.builder()
                        .status(HttpStatus.OK)
                        .message("Success")
                        .build()
        );
    }

    @PutMapping("/approve/{itemId}")
    public ResponseEntity<?> approveItem(
            @PathVariable Integer itemId,
            @Valid @RequestBody ItemApprove itemApprove,
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
        itemService.approve(itemId, itemApprove);
        return ResponseEntity.ok(
                ResponseObject.builder()
                        .status(HttpStatus.OK)
                        .message("Success")
                        .build()
        );
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<?> deleteItem(
            @PathVariable Integer itemId
    ) throws Exception {
        itemService.deleteItem(itemId);
        return ResponseEntity.ok(
                ResponseObject.builder()
                        .status(HttpStatus.OK)
                        .message("Success")
                        .build()
        );
    }

    @GetMapping("top-10-featured-item")
    public ResponseEntity<?> getTop10FeaturedItem() throws Exception {
        List<AuctionItemResponse> itemResponseList = itemService.getTop10FeaturedItem();
        return ResponseEntity.ok(
                ResponseObject.builder()
                        .status(HttpStatus.OK)
                        .message("Success")
                        .data(itemResponseList)
                        .build()
        );
    }

    @GetMapping("product-appraisal")
    public ResponseEntity<?> getProductAppraisal(
            @RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value = "limit", defaultValue = "10") int limit
    ) throws Exception {
        PageRequest pageRequest = PageRequest.of(page, limit);
        //, Sort.by("id").descending()
        Page<AuctionItemResponse> itemResponseList = itemService.getProductAppraisal(pageRequest);
        int totalPages = itemResponseList.getTotalPages();
        Long totalOrder = itemResponseList.getTotalElements();
        List<AuctionItemResponse> auctionItemResponses = itemResponseList.getContent();
        ResponseListObject<List<AuctionItemResponse>> responseListObject = ResponseListObject.<List<AuctionItemResponse>>builder()
                .data(auctionItemResponses)
                .totalElements(totalOrder)
                .totalPages(totalPages)
                .build();
        return ResponseEntity.ok(
                ResponseObject.builder()
                        .status(HttpStatus.OK)
                        .message("Success")
                        .data(responseListObject)
                        .build()
        );
    }

    @GetMapping("")
    public ResponseEntity<?> getItem(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice,
            @RequestParam(value = "scIds", required = false) String scIds,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) throws Exception {
        List<Integer> parsedScIds = parseIds(scIds);
        PageRequest pageRequest = PageRequest.of(page, limit);
        //, Sort.by("id").descending()
        Page<AuctionItemResponse> itemResponseList = itemService.getItem(keyword, minPrice, maxPrice, pageRequest, parsedScIds);
        int totalPages = itemResponseList.getTotalPages();
        Long totalOrder = itemResponseList.getTotalElements();
        List<AuctionItemResponse> auctionItemResponses = itemResponseList.getContent();
        ResponseListObject<List<AuctionItemResponse>> responseListObject = ResponseListObject.<List<AuctionItemResponse>>builder()
                .data(auctionItemResponses)
                .totalElements(totalOrder)
                .totalPages(totalPages)
                .build();
        return ResponseEntity.ok(
                ResponseObject.builder()
                        .status(HttpStatus.OK)
                        .message("Success")
                        .data(responseListObject)
                        .build()
        );
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<?> getItemDetail(@PathVariable Integer id) throws Exception {
        ItemDetailResponse itemDetailResponse = itemService.getItemById(id);
        return ResponseEntity.ok(
                ResponseObject.builder()
                        .status(HttpStatus.OK)
                        .message("Success")
                        .data(itemDetailResponse)
                        .build()
        );
    }

    @GetMapping("/auction-process/user")
    public ResponseEntity<?> getAuctionProcessByUser(
            //@RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) throws Exception {
        PageRequest pageRequest = PageRequest.of(page, limit);
        //, Sort.by("id").descending()
        Page<AuctionItemResponse> auctionItemResponses = itemService.getAuctionProcess(pageRequest);
        int totalPages = auctionItemResponses.getTotalPages();
        Long totalOrder = auctionItemResponses.getTotalElements();
        List<AuctionItemResponse> itemResponseList = auctionItemResponses.getContent();
        ResponseListObject<List<AuctionItemResponse>> responseListObject = ResponseListObject.<List<AuctionItemResponse>>builder()
                .data(itemResponseList)
                .totalElements(totalOrder)
                .totalPages(totalPages)
                .build();
        return ResponseEntity.ok(
                ResponseObject.builder()
                        .status(HttpStatus.OK)
                        .message("Success")
                        .data(responseListObject)
                        .build()
        );
    }


    private List<Integer> parseIds(String ids) {
        if (ids == null || ids.isEmpty()) {
            return null;
        }
        return Arrays.stream(ids.split(",")).map(Integer::parseInt).collect(Collectors.toList());
    }
}
