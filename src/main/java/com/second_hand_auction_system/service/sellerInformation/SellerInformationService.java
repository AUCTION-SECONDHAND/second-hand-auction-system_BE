package com.second_hand_auction_system.service.sellerInformation;

import com.second_hand_auction_system.converters.sellerInformation.SellerInformationConverter;
import com.second_hand_auction_system.dtos.request.sellerInfomation.SellerInformationDto;
import com.second_hand_auction_system.dtos.responses.feedback.FeedbackResponse;
import com.second_hand_auction_system.dtos.responses.sellerInformation.SellerInformationResponse;
import com.second_hand_auction_system.models.FeedBack;
import com.second_hand_auction_system.models.Item;
import com.second_hand_auction_system.models.SellerInformation;
import com.second_hand_auction_system.models.User;
import com.second_hand_auction_system.repositories.FeedbackRepository;
import com.second_hand_auction_system.repositories.ItemRepository;
import com.second_hand_auction_system.repositories.SellerInformationRepository;
import com.second_hand_auction_system.repositories.UserRepository;
import com.second_hand_auction_system.service.feedback.IFeedbackService;
import com.second_hand_auction_system.service.jwt.IJwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SellerInformationService implements ISellerInformationService {

    private final SellerInformationRepository sellerInformationRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final IFeedbackService feedbackService;
    private final FeedbackRepository feedbackRepository;
    private final IJwtService jwtService;

    @Override
    public SellerInformationResponse createSellerInformation(SellerInformationDto sellerInformationDto, Integer userId) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + userId));

        SellerInformation sellerInformation = SellerInformationConverter.convertToEntity(sellerInformationDto, user);
        SellerInformation savedSellerInfo = sellerInformationRepository.save(sellerInformation);

        return SellerInformationConverter.convertToResponse(savedSellerInfo);
    }

    @Override
    public SellerInformationResponse updateSellerInformation(SellerInformationDto sellerInformationDto) throws Exception {
        // Extract the Authorization header
        String authHeader = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
                .getRequest().getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new Exception("Unauthorized: Missing or invalid Authorization header");
        }

        // Extract the JWT token and user email
        String token = authHeader.substring(7);
        String userEmail = jwtService.extractUserEmail(token);

        // Find the authenticated user
        User requester = userRepository.findByEmailAndStatusIsTrue(userEmail)
                .orElseThrow(() -> new Exception("User not found or inactive"));

        // Retrieve the seller information associated with the user
        SellerInformation existingSellerInfo = sellerInformationRepository.findByUser_Id(requester.getId())
                .orElseThrow(() -> new NoSuchElementException("Seller information not found for user ID: " + requester.getId()));

        // Update fields only if they are not null or empty
        if (sellerInformationDto.getStoreName() != null && !sellerInformationDto.getStoreName().trim().isEmpty()) {
            existingSellerInfo.setStoreName(sellerInformationDto.getStoreName());
        }

        if (sellerInformationDto.getAddress() != null && !sellerInformationDto.getAddress().trim().isEmpty()) {
            existingSellerInfo.setAddress(sellerInformationDto.getAddress());
        }

        if (sellerInformationDto.getDescription() != null && !sellerInformationDto.getDescription().trim().isEmpty()) {
            existingSellerInfo.setDescription(sellerInformationDto.getDescription());
        }

        if (sellerInformationDto.getAvatar() != null && !sellerInformationDto.getAvatar().trim().isEmpty()) {
            existingSellerInfo.setAvatar(sellerInformationDto.getAvatar());
        }

        if (sellerInformationDto.getBackgroundImage() != null && !sellerInformationDto.getBackgroundImage().trim().isEmpty()) {
            existingSellerInfo.setBackgroundImage(sellerInformationDto.getBackgroundImage());
        }

        // Save the updated seller information
        SellerInformation updatedSellerInfo = sellerInformationRepository.save(existingSellerInfo);

        // Convert the updated entity to a response DTO and return it
        return SellerInformationConverter.convertToResponse(updatedSellerInfo);
    }



    @Override
    public SellerInformationResponse getSellerInformationById(Integer sellerId) throws Exception {
        SellerInformation sellerInformation = sellerInformationRepository.findById(sellerId)
                .orElseThrow(() -> new NoSuchElementException("Seller information not found with ID: " + sellerId));

        return SellerInformationConverter.convertToResponse(sellerInformation);
    }

    @Override
    public void deleteSellerInformation(Integer sellerId) throws Exception {
        SellerInformation sellerInformation = sellerInformationRepository.findById(sellerId)
                .orElseThrow(() -> new NoSuchElementException("Seller information not found with ID: " + sellerId));

        sellerInformationRepository.delete(sellerInformation);
    }

    @Override
    public SellerInformationResponse getSellerInformationByUserId(Integer userId) throws Exception {
        SellerInformation sellerInformation = sellerInformationRepository.findByUser_Id(userId)
                .orElseThrow(() -> new NoSuchElementException("Seller information not found for user ID: " + userId));

        Page<FeedbackResponse> feedbackResponses = feedbackService.getFeedbackBySellerUserId(userId, 0, 10);

        return SellerInformationConverter.convertToResponseWithFeedback(sellerInformation, feedbackResponses);
    }


    @Override
    public SellerInformationResponse getSellerInformationByAuctionId(Integer auctionId) throws Exception {
        Item item = itemRepository.findByAuctions_AuctionId(auctionId);
        if (item == null) {
            throw new NoSuchElementException("Item not found for auctionId: " + auctionId);
        }

        SellerInformation sellerInformation = sellerInformationRepository.findByUser_Id(item.getUser().getId())
                .orElseThrow(() -> new NoSuchElementException("Seller information not found for user ID: " + item.getUser().getId()));

        Page<FeedbackResponse> feedbackResponses = feedbackService.getFeedbackBySellerUserId(item.getUser().getId(), 0, 10);

        return SellerInformationConverter.convertToResponseWithFeedback(sellerInformation, feedbackResponses);
    }

    @Override
    public ResponseEntity<?> findTop5() {
        List<FeedBack> feedBack = feedbackRepository.findAll();  // Lấy danh sách tất cả các phản hồi
        // Kiểm tra nếu feedBack không rỗng
        if (feedBack.isEmpty()) {
            return ResponseEntity.ok(new ArrayList<>());
        }
        // Map để lưu trữ số lượng feedback cho mỗi seller
        Map<Integer, Long> sellerFeedbackCountMap = new HashMap<>();
        // Duyệt qua các feedBack và tính số lượng feedback cho mỗi seller
        for (FeedBack feedback : feedBack) {
            User seller = feedback.getItem().getUser();
            sellerFeedbackCountMap.put(seller.getId(),
                    sellerFeedbackCountMap.getOrDefault(seller.getId(), 0L) + 1);
        }
        // Sắp xếp danh sách sellers theo số lượng feedback giảm dần và lấy 5 seller đầu tiên
        List<Map.Entry<Integer, Long>> sortedSellerList = sellerFeedbackCountMap.entrySet().stream()
                .sorted((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue())) // Sắp xếp giảm dần
                .limit(5) // Lấy top 5
                .toList();
        // Tạo danh sách phản hồi cho 5 seller top
        List<SellerInformationResponse> top5Sellers = new ArrayList<>();
        for (Map.Entry<Integer, Long> entry : sortedSellerList) {
            Integer sellerId = entry.getKey();
            SellerInformation sellerInformation = sellerInformationRepository.findById(sellerId).orElse(null);

            // Nếu seller có thông tin, thêm vào danh sách kết quả
            if (sellerInformation != null) {
                SellerInformationResponse response = SellerInformationConverter.convertToResponse(sellerInformation);
                response.setTotalFeedbackCount(entry.getValue().intValue()); // Chuyển đổi Long thành int
                top5Sellers.add(response);
            }
        }
        // Trả về danh sách seller top 5
        return ResponseEntity.ok(top5Sellers);
    }

    @Override
    public SellerInformationResponse getSellerInformationByToken() throws Exception {
        // Extract the token from the request header
        String authHeader = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
                .getRequest().getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new Exception("Unauthorized");
        }

        // Extract the token without the "Bearer " prefix
        String token = authHeader.substring(7);

        // Extract the user email from the token
        String userEmail = jwtService.extractUserEmail(token);

        // Find the user by email and ensure the user is active
        User requester = userRepository.findByEmailAndStatusIsTrue(userEmail).orElse(null);
        if (requester == null) {
            throw new Exception("User not found");
        }

        // Retrieve the seller information associated with the user
        SellerInformation sellerInformation = sellerInformationRepository.findByUser_Id(requester.getId())
                .orElseThrow(() -> new Exception("Seller information not found"));

        // Convert the seller information entity to response DTO
        SellerInformationResponse response = SellerInformationConverter.convertToResponse(sellerInformation);

        return response;
    }


}








