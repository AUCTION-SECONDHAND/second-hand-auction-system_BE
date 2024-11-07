package com.second_hand_auction_system.service.item;

import com.second_hand_auction_system.converters.item.AuctionItemConvert;
import com.second_hand_auction_system.dtos.request.item.ImgItemDto;
import com.second_hand_auction_system.dtos.request.item.ItemApprove;
import com.second_hand_auction_system.dtos.request.item.ItemDto;
import com.second_hand_auction_system.dtos.responses.ResponseObject;
import com.second_hand_auction_system.dtos.responses.item.*;
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

import java.util.*;
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
    private final AuctionTypeRepository auctionTypeRepository;

    @Override
    @Transactional
    public void addItem(ItemDto itemDto) throws Exception {
        Item item = modelMapper.map(itemDto, Item.class);

        SubCategory subCategory = subCategoryRepository.findById(itemDto.getScId())
                .orElseThrow(() -> new DataNotFoundException("SubCategory not found with id: " + itemDto.getScId()));
        AuctionType auctionTypeExisted = auctionTypeRepository.findById(itemDto.getAuctionType())
                .orElseThrow(() -> new DataNotFoundException("AuctionType not found with id: " + itemDto.getAuctionType()));
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
        item.setAuctionType(auctionTypeExisted);
        item.setUser(requester);
        item.setCreateBy(requester.getFullName());
        item.setUpdateBy(requester.getFullName());
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
                imageItem.setImageUrl(imgItemDto.getImageUrl());
                imageItem.setItem(item);
                imageItems.add(imageItem);
                // Set the first image as thumbnail
                if (i == 0) {
                    item.setThumbnail(imgItemDto.getImageUrl());
                }
            }

            // Save all image items in one go
            imageItemRepository.saveAll(imageItems);
            item.setImageItems(imageItems);
            item.setThumbnail(imgItemDtos.get(0).getImageUrl());

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
        var item = itemRepository.findById(itemId).orElseThrow(null);
        if(item== null){
            throw new Exception("Item not found");
        }
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
//        Pageable pageable = PageRequest.of(0, 10);
        List<Item> items = itemRepository.findAll();
        return items.stream()
                .map(auctionItemConvert::toAuctionItemResponse)
                .collect(Collectors.toList());
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
        String authHeader = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
                .getRequest().getHeader("Authorization");

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
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("items", items.map(auctionItemConvert::toAuctionItemResponse).getContent());
        responseData.put("totalPages", items.getTotalPages());
        responseData.put("totalElements", items.getTotalElements());

        return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder()
                .status(HttpStatus.OK)
                .message("Success")
                .data(responseData)
                .build());
    }


    @Override
    public AuctionItemResponse getAuctionItemById(int itemId) throws Exception {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new DataNotFoundException("Item not found"));
        AuctionItemResponse auctionItemResponse = auctionItemConvert.toAuctionItemResponse(item);
        return auctionItemResponse;
    }

    @Override
    public ResponseEntity<?> getItemByUser(int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        String authHeader = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
                .getRequest().getHeader("Authorization");

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
        Page<Item> items = itemRepository.findItemByUser_Id(requester.getId(),pageable);
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("items", items.map(auctionItemConvert::toAuctionItemResponse).getContent());
        responseData.put("totalPages", items.getTotalPages());
        responseData.put("totalElements", items.getTotalElements());

        return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder()
                .status(HttpStatus.OK)
                .message("Success")
                .data(responseData)
                .build());
    }

    @Override
    public ResponseEntity<?> getItemByCondition() {
        return null;
    }

    @Override
    public ResponseEntity<?> getItemPending(int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<Item> listItemPending = itemRepository.findAllByItemStatus(ItemStatus.PENDING, pageable);

        List<ItemResponse2> itemResponses = listItemPending.getContent().stream()
                .map(item -> ItemResponse2.builder()
                        .itemId(item.getItemId())
                        .itemName(item.getItemName())
                        .brandName(item.getBrandName())
                        .createBy(item.getCreateBy())
                        .itemSpecific(item.getItemSpecific() != null ?
                                ItemSpecificResponse.builder() // Đảm bảo sử dụng đúng lớp
                                        .itemSpecId(item.getItemSpecific().getItemSpecificId())
                                        .color(item.getItemSpecific().getColor())
                                        .manufactureDate(item.getItemSpecific().getManufactureDate())
                                        .type(item.getItemSpecific().getType())
                                        .weight(item.getItemSpecific().getWeight())
                                        .percent(item.getItemSpecific().getPercent())
                                        .material(item.getItemSpecific().getMaterial())
                                        .dimension(item.getItemSpecific().getDimension())
                                        .original(item.getItemSpecific().getOriginal())
                                        .build() : null)
                        .itemCondition(String.valueOf(item.getItemCondition()))
                        .itemDescription(item.getItemDescription())
                        .thumbnail(item.getThumbnail())
                        .itemStatus(item.getItemStatus())
                        .create_at(item.getCreateAt())
                        .update_at(item.getUpdateAt())
                        .build())
                .toList();

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("items", itemResponses);
        responseData.put("totalPages", listItemPending.getTotalPages());
        responseData.put("totalElements", listItemPending.getTotalElements());

        return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder()
                .status(HttpStatus.OK)
                .message("Success")
                .data(responseData)
                .build());
    }

    @Override
    public ResponseEntity<?> getTop10ItemParticipating() {
        List<Item> itemList = itemRepository.findTop10ItemsWithMostBids(PageRequest.of(0, 10));
        List<AuctionItemResponse> auctionItemResponses = new ArrayList<>();
        for (Item item : itemList) {
            AuctionItemResponse auctionItemResponse = auctionItemConvert.toAuctionItemResponse(item);
            auctionItemResponses.add(auctionItemResponse);
        }
        return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder()
                        .data(auctionItemResponses)
                        .message("Success")
                        .status(HttpStatus.OK)
                .build());
    }


    public Integer extractUserIdFromToken(String token) throws Exception {
        String userEmail = jwtService.extractUserEmail(token); // Extract email from token
        User user = userRepository.findByEmail(userEmail) // Find user by email
                .orElseThrow(() -> new Exception("User not found!!!"));
        return user.getId();
    }
}
