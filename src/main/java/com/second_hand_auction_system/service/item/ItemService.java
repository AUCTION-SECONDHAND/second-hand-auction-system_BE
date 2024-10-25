package com.second_hand_auction_system.service.item;

import com.second_hand_auction_system.converters.item.AuctionItemConvert;
import com.second_hand_auction_system.dtos.request.item.ImgItemDto;
import com.second_hand_auction_system.dtos.request.item.ItemApprove;
import com.second_hand_auction_system.dtos.request.item.ItemDto;
import com.second_hand_auction_system.dtos.responses.ResponseObject;
import com.second_hand_auction_system.dtos.responses.item.AuctionItemResponse;
import com.second_hand_auction_system.dtos.responses.item.ItemDetailResponse;
import com.second_hand_auction_system.exceptions.DataNotFoundException;
import com.second_hand_auction_system.models.*;
import com.second_hand_auction_system.repositories.*;
import com.second_hand_auction_system.service.email.EmailService;
import com.second_hand_auction_system.service.jwt.IJwtService;
import com.second_hand_auction_system.utils.AuctionStatus;
import com.second_hand_auction_system.utils.ItemStatus;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemService implements IItemService {
    private final ItemRepository itemRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final ImageItemRepository imageItemRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final ItemSpecificRepository itemSpecificRepository;
    private final IJwtService jwtService;
    private final EmailService emailService;
    private final AuctionRepository auctionRepository;
    private final AuctionItemConvert auctionItemConvert;

    @Override
    @Transactional
    public void addItem(ItemDto itemDto) throws Exception {
        Item item = modelMapper.map(itemDto, Item.class);

        SubCategory subCategory = subCategoryRepository.findById(itemDto.getScId())
                .orElseThrow(() -> new DataNotFoundException("SubCategory not found with id: " + itemDto.getScId()));
        String authHeader = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest().getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new Exception("Unauthorized");
        }
        String token = authHeader.substring(7);
        String userEmail = jwtService.extractUserEmail(token);
        User requester = userRepository.findByEmailAndStatusIsTrue(userEmail).orElse(null);
        if (requester == null) {
            throw new Exception("User not found");
        }
        item.setItemStatus(ItemStatus.PENDING);
        item.setSubCategory(subCategory);
        item.setUser(requester);
        item.setCreateBy(requester.getUsername());
        item.setUpdateBy(requester.getUsername());
        if (itemDto.getItemSpecific() != null) {

            ItemSpecific itemSpecific = modelMapper.map(itemDto.getItemSpecific(), ItemSpecific.class);
            itemSpecific.setItem(item);
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
//        User userExist = userRepository.findById(itemDto.getUserId())
//                .orElseThrow(() -> new DataNotFoundException("User not found with id: " + itemDto.getUserId()));
        String authHeader = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest().getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new Exception("Unauthorized");
        }
        String token = authHeader.substring(7);
        String userEmail = jwtService.extractUserEmail(token);
        User requester = userRepository.findByEmailAndStatusIsTrue(userEmail).orElse(null);
        if (requester == null) {
            throw new Exception("User not found");
        }
        modelMapper.map(itemDto, itemExist);
        itemExist.setSubCategory(subCategory);
        itemExist.setUser(requester);
        itemExist.setCreateBy(requester.getUsername());
        itemExist.setUpdateBy(requester.getUsername());
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

    @Override
    public void approve(int itemId, ItemApprove approve) throws Exception {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new DataNotFoundException("Item not found"));
        String authHeader = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest().getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new Exception("Unauthorized");
        }
        String token = authHeader.substring(7);
        String userEmail = jwtService.extractUserEmail(token);
        User requester = userRepository.findByEmailAndStatusIsTrue(userEmail).orElse(null);
        if (requester == null) {
            throw new Exception("User not found");
        }
        item.setUpdateBy(requester.getUsername());
        item.setItemStatus(approve.getStatus());
        if (item.getItemSpecific() != null) {
            ItemSpecific itemSpecificExist = itemSpecificRepository.findById(item.getItemSpecific().getItemSpecificId())
                    .orElseThrow(() -> new DataNotFoundException("Item not found"));
            modelMapper.map(item.getItemSpecific(), itemSpecificExist);
            item.setItemSpecific(itemSpecificExist);
        }
        itemRepository.save(item);
        emailService.sendNotificationRegisterItem(userEmail, item.getUser().getFullName()
                , item.getItemName());
    }

    public List<AuctionItemResponse> getTop10FeaturedItem() {
        List<Item> items = itemRepository.findAll();
        return items.stream()
                .map(auctionItemConvert::toAuctionItemResponse)
                .collect(Collectors.toList());
//        return items.map(auctionItemConvert::toAuctionItemResponse);
    }

    @Override
    public Page<AuctionItemResponse> getProductAppraisal(PageRequest pageRequest) throws Exception {
        Page<Item> items = itemRepository.findAllByItemStatus(ItemStatus.ACCEPTED, pageRequest);
        return items.map(auctionItemConvert::toAuctionItemResponse);
    }

    @Override
    public Page<AuctionItemResponse> getItem(String keyword, Double minPrice, Double maxPrice, PageRequest pageRequest, List<Integer> subCategoryIds) throws Exception {
        Page<Item> items;
        items = itemRepository.searchItems(
                keyword, minPrice, maxPrice, subCategoryIds, pageRequest
        );
        return items.map(auctionItemConvert::toAuctionItemResponse);
    }

    @Override
    public ItemDetailResponse getItemById(int itemId) throws Exception {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new DataNotFoundException("Item not found"));
        ItemDetailResponse itemDetailResponse = auctionItemConvert.toAuctionDetailItemResponse(item);
        return itemDetailResponse;
    }

    @Override
    public Page<AuctionItemResponse> getAuctionProcess(PageRequest pageRequest) throws Exception {
        String token = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
                .getRequest().getHeader("Authorization").substring(7);
        Integer userId = extractUserIdFromToken(token);
        Page<Item> items = itemRepository.findAllByAuction_StatusAndUserId(AuctionStatus.PENDING, userId, pageRequest);
        return items.map(auctionItemConvert::toAuctionItemResponse);
    }

    @Override
    public ResponseEntity<?> getItemAuctionCompleted(int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        String authHeader = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest().getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResponseObject.builder()
                            .status(HttpStatus.UNAUTHORIZED)
                            .message("Unauthorized")
                            .data(null)
                    .build());
        }
        String token = authHeader.substring(7);
        String userEmail = jwtService.extractUserEmail(token);
        User requester = userRepository.findByEmailAndStatusIsTrue(userEmail).orElse(null);
        if (requester == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                    .status(HttpStatus.NOT_FOUND)
                    .message("User not found")
                    .data(null)
                    .build());
        }
        Page<Item> items = itemRepository.findAllByUserIdAndAuctionStatus(requester.getId(), AuctionStatus.COMPLETED, pageable);

        return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder()
                .status(HttpStatus.OK)
                .message("Success")
                .data(items.map(auctionItemConvert::toAuctionItemResponse))
                .build());
    }

    public Integer extractUserIdFromToken(String token) throws Exception {
        String userEmail = jwtService.extractUserEmail(token); // Extract email from token
        User user = userRepository.findByEmail(userEmail) // Find user by email
                .orElseThrow(() -> new Exception("User not found!!!"));
        return user.getId();
    }
}
