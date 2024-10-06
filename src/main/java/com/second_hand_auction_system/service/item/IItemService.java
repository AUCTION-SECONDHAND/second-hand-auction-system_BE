package com.second_hand_auction_system.service.item;

import com.second_hand_auction_system.dtos.request.item.ItemDto;

public interface IItemService {
    void addItem(ItemDto itemDto) throws Exception;

    void updateItem(int itemId, ItemDto itemDto) throws Exception;

    void deleteItem(int itemId) throws Exception;
}
