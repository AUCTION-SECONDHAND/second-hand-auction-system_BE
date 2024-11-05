package com.second_hand_auction_system.controller;

import com.second_hand_auction_system.dtos.request.feedback.FeedbackDto;
import com.second_hand_auction_system.dtos.responses.feedback.FeedbackResponse;
import com.second_hand_auction_system.service.feedback.IFeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final IFeedbackService feedbackService;

    @PostMapping
    public ResponseEntity<FeedbackResponse> createFeedback(@RequestBody FeedbackDto feedbackDto) throws Exception {
        FeedbackResponse feedbackResponse = feedbackService.createFeedback(feedbackDto);
        return ResponseEntity.ok(feedbackResponse);
    }

    @PutMapping("/{feedbackId}")
    public ResponseEntity<FeedbackResponse> updateFeedback(@PathVariable Integer feedbackId,
                                                           @RequestBody FeedbackDto feedbackDto) throws Exception {
        FeedbackResponse feedbackResponse = feedbackService.updateFeedback(feedbackId, feedbackDto);
        return ResponseEntity.ok(feedbackResponse);
    }

    @DeleteMapping("/{feedbackId}")
    public ResponseEntity<Void> deleteFeedback(@PathVariable Integer feedbackId) throws Exception {
        feedbackService.deleteFeedback(feedbackId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{feedbackId}")
    public ResponseEntity<FeedbackResponse> getFeedbackById(@PathVariable Integer feedbackId) throws Exception {
        FeedbackResponse feedbackResponse = feedbackService.getFeedbackById(feedbackId);
        return ResponseEntity.ok(feedbackResponse);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FeedbackResponse>> getAllFeedbacksByUserId(@PathVariable Integer userId) throws Exception {
        List<FeedbackResponse> feedbackResponses = feedbackService.getAllFeedbacksByUserId(userId);
        return ResponseEntity.ok(feedbackResponses);
    }

    @GetMapping("/item/{itemId}")
    public ResponseEntity<List<FeedbackResponse>> getAllFeedbacksByItemId(@PathVariable Integer itemId) throws Exception {
        List<FeedbackResponse> feedbackResponses = feedbackService.getAllFeedbacksSellerId(itemId);
        return ResponseEntity.ok(feedbackResponses);
    }


    @GetMapping("/seller/{userId}")
    public ResponseEntity<Page<FeedbackResponse>> getFeedbackBySellerUserId(@PathVariable Integer userId,
                                                                            @RequestParam(defaultValue = "0") int page,
                                                                            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<FeedbackResponse> responses = feedbackService.getFeedbackBySellerUserId(userId, page, size);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }


}