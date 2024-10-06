package com.second_hand_auction_system.service.item;

import com.second_hand_auction_system.dtos.request.item.ImgItemDto;
import com.second_hand_auction_system.dtos.request.item.ItemDto;
import com.second_hand_auction_system.dtos.request.item.ItemSpecificDto;
import com.second_hand_auction_system.exceptions.DataNotFoundException;
import com.second_hand_auction_system.models.*;
import com.second_hand_auction_system.repositories.*;
import com.second_hand_auction_system.utils.ItemStatus;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService implements IItemService {
    private final ItemRepository itemRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final ImageItemRepository imageItemRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final ItemSpecificRepository itemSpecificRepository;

    @Override
    @Transactional
    public void addItem(ItemDto itemDto) throws Exception {
        Item item = modelMapper.map(itemDto, Item.class);
        SubCategory subCategory = subCategoryRepository.findById(itemDto.getScId())
                .orElseThrow(() -> new DataNotFoundException("SubCategory not found with id: " + itemDto.getScId()));
        User userExist = userRepository.findById(itemDto.getUserId())
                .orElseThrow(() -> new DataNotFoundException("User not found with id: " + itemDto.getUserId()));

        item.setSubCategory(subCategory);
        item.setUser(userExist);
        item.setCreateBy(userExist.getUsername());
        item.setUpdateBy(userExist.getUsername());
        if (itemDto.getItemSpecific() != null) {
            ItemSpecific itemSpecific = modelMapper.map(itemDto.getItemSpecific(), ItemSpecific.class);
            item.setItemSpecific(itemSpecific);
        }
        if (itemDto.getImgItem() != null && !itemDto.getImgItem().isEmpty()) {
            List<ImgItemDto> imgItemDtos = itemDto.getImgItem();
            // Limit to 5 images
            if (imgItemDtos.size() > 5) {
                throw new Exception("Cannot upload more than 5 images.");
            }
            List<ImageItem> imageItems = new ArrayList<>();
            for (int i = 0; i < imgItemDtos.size(); i++) {
                ImgItemDto imgItemDto = imgItemDtos.get(i);
                ImageItem imageItem = new ImageItem();
                imageItem.setImageUrl(imgItemDto.getImageUrl()); // Set image URL
                imageItem.setItem(item);  // Set the relationship with Item
                imageItems.add(imageItem);

                // Set the first image as thumbnail
                if (i == 0) {
                    item.setThumbnail(imgItemDto.getImageUrl());
                }
            }
            // Save all image items in one go
            imageItemRepository.saveAll(imageItems);
            // Optionally, associate the list of image items back to the item object
            item.setImageItems(imageItems);
        }
        itemRepository.save(item);
    }

    @Override
    public void updateItem(int itemId, ItemDto itemDto) throws Exception {
        Item itemExist = itemRepository.findById(itemId)
                .orElseThrow(() -> new DataNotFoundException("Item not found"));
        SubCategory subCategory = subCategoryRepository.findById(itemDto.getScId())
                .orElseThrow(() -> new DataNotFoundException("SubCategory not found with id: " + itemDto.getScId()));
        User userExist = userRepository.findById(itemDto.getUserId())
                .orElseThrow(() -> new DataNotFoundException("User not found with id: " + itemDto.getUserId()));
        modelMapper.map(itemDto, itemExist);
        itemExist.setSubCategory(subCategory);
        itemExist.setUser(userExist);
        itemExist.setCreateBy(userExist.getUsername());
        itemExist.setUpdateBy(userExist.getUsername());
        if (itemDto.getItemSpecific() != null) {
            ItemSpecific itemSpecificExist = itemSpecificRepository.findById(itemExist.getItemSpecific().getItemSpecificId())
                    .orElseThrow(() -> new DataNotFoundException("Item not found"));
            modelMapper.map(itemDto.getItemSpecific(), itemSpecificExist);
            itemExist.setItemSpecific(itemSpecificExist);
        }
        itemRepository.save(itemExist);
    }

    @Override
    public void deleteItem(int itemId) throws Exception {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new DataNotFoundException("Item not found"));
        item.setItemStatus(ItemStatus.INACTIVE);
        itemRepository.save(item);
    }
}
