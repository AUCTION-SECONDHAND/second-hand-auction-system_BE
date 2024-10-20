package com.second_hand_auction_system.service.item;

import com.second_hand_auction_system.dtos.request.item.ItemApprove;
import com.second_hand_auction_system.dtos.request.item.ItemDto;
import com.second_hand_auction_system.dtos.responses.item.AuctionItemResponse;

import java.util.List;

public interface IItemService {
    void addItem(ItemDto itemDto) throws Exception;

    void updateItem(int itemId, ItemDto itemDto) throws Exception;

    void deleteItem(int itemId) throws Exception;

    void approve(int itemId, ItemApprove approve) throws Exception;
    List<AuctionItemResponse> getTop10FeaturedItem() throws Exception;
}
