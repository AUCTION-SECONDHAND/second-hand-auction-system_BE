package com.second_hand_auction_system.service.item;

import com.second_hand_auction_system.dtos.request.item.ItemApprove;
import com.second_hand_auction_system.dtos.request.item.ItemDto;
import com.second_hand_auction_system.dtos.responses.item.AuctionItemResponse;
import com.second_hand_auction_system.dtos.responses.item.ItemDetailResponse;
import com.second_hand_auction_system.models.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface IItemService {
    void addItem(ItemDto itemDto) throws Exception;

    void updateItem(int itemId, ItemDto itemDto) throws Exception;

    void deleteItem(int itemId) throws Exception;

    void approve(int itemId, ItemApprove approve) throws Exception;
    List<AuctionItemResponse> getTop10FeaturedItem() throws Exception;
    Page<AuctionItemResponse> getProductAppraisal(PageRequest pageRequest) throws Exception;
    Page<AuctionItemResponse> getItem(
            String keyword, Double minPrice,
            Double maxPrice,
            PageRequest pageRequest,
            List<Integer> subCategoryIds
    ) throws Exception;

    ItemDetailResponse getItemById(int itemId) throws Exception;
    Page<AuctionItemResponse> getAuctionProcess(PageRequest pageRequest) throws Exception;

    ResponseEntity<?> getItemAuctionCompleted(int page, int limit);
    AuctionItemResponse getAuctionItemById(int itemId) throws Exception;
}
