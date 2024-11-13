package com.second_hand_auction_system.converters.sellerInformation;

import com.second_hand_auction_system.dtos.request.sellerInfomation.SellerInformationDto;
import com.second_hand_auction_system.dtos.responses.feedback.FeedbackResponse;
import com.second_hand_auction_system.dtos.responses.sellerInformation.SellerInformationResponse;
import com.second_hand_auction_system.models.SellerInformation;
import com.second_hand_auction_system.models.User;
import com.second_hand_auction_system.service.feedback.IFeedbackService;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class SellerInformationConverter {


    public static SellerInformation convertToEntity(SellerInformationDto sellerInformationDto, User user) {
        return SellerInformation.builder()
                .storeName(sellerInformationDto.getStoreName())
                .address(sellerInformationDto.getAddress())
                .description(sellerInformationDto.getDescription())
                .avatar(sellerInformationDto.getAvatar())
                .backgroundImage(sellerInformationDto.getBackgroundImage())
                .user(user)
                .build();
    }

    public static SellerInformationResponse convertToResponse(SellerInformation sellerInformation) {
        return SellerInformationResponse.builder()
                .sellerId(sellerInformation.getSellerId())
                .storeName(sellerInformation.getStoreName())
                .address(sellerInformation.getAddress())
                .description(sellerInformation.getDescription())
                .avatar(sellerInformation.getAvatar())
                .backgroundImage(sellerInformation.getBackgroundImage())
                .userId(sellerInformation.getUser().getId())
                .build();
    }

    public static SellerInformationResponse convertToResponseWithFeedback(SellerInformation sellerInformation, Page<FeedbackResponse> feedbackResponses) {
        int totalFeedbackCount = (int) feedbackResponses.getTotalElements();
        double totalStars = totalFeedbackCount > 0
                ? Math.min(5.0, feedbackResponses.stream()
                .mapToInt(FeedbackResponse::getRating)
                .average().orElse(0.0))
                : 0.0;

        double[] ratingPercentages = new double[5];
        Arrays.fill(ratingPercentages, 0.0);

        feedbackResponses.getContent().forEach(feedback -> {
            int rating = feedback.getRating();
            if (rating >= 1 && rating <= 5) {
                ratingPercentages[rating - 1] += 1;
            }
        });

        // Chuyển số lượng thành tỷ lệ phần trăm
        if (totalFeedbackCount > 0) {
            for (int i = 0; i < ratingPercentages.length; i++) {
                ratingPercentages[i] = (ratingPercentages[i] / totalFeedbackCount) * 100;
            }
        }

        List<FeedbackResponse> feedbackList = feedbackResponses.getContent().stream()
                .limit(3)
                .collect(Collectors.toList());

        return SellerInformationResponse.builder()
                .sellerId(sellerInformation.getSellerId())
                .storeName(sellerInformation.getStoreName())
                .address(sellerInformation.getAddress())
                .description(sellerInformation.getDescription())
                .avatar(sellerInformation.getAvatar())
                .backgroundImage(sellerInformation.getBackgroundImage())
                .userId(sellerInformation.getUser().getId())
                .totalFeedbackCount(totalFeedbackCount)
                .totalStars(totalStars)
                .feedbackList(feedbackList)
                .rating1Percentage(ratingPercentages[0])
                .rating2Percentage(ratingPercentages[1])
                .rating3Percentage(ratingPercentages[2])
                .rating4Percentage(ratingPercentages[3])
                .rating5Percentage(ratingPercentages[4])
                .sellerCreateAt(sellerInformation.getUser().getCreateAt())
                .build();
    }


}
