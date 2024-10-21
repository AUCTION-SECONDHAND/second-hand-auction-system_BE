package com.second_hand_auction_system.controller;

import com.second_hand_auction_system.dtos.request.feedback.FeedbackDto;
import com.second_hand_auction_system.dtos.responses.feedback.FeedbackResponses;
import com.second_hand_auction_system.service.feedback.IFeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final IFeedbackService feedbackService;

    @PostMapping
    public ResponseEntity<FeedbackResponses> createFeedback(@RequestBody FeedbackDto feedbackDto) throws Exception {
        FeedbackResponses feedbackResponse = feedbackService.createFeedback(feedbackDto);
        return ResponseEntity.ok(feedbackResponse);
    }

    @PutMapping("/{feedbackId}")
    public ResponseEntity<FeedbackResponses> updateFeedback(@PathVariable Integer feedbackId,
                                                            @RequestBody FeedbackDto feedbackDto) throws Exception {
        FeedbackResponses feedbackResponse = feedbackService.updateFeedback(feedbackId, feedbackDto);
        return ResponseEntity.ok(feedbackResponse);
    }

    @DeleteMapping("/{feedbackId}")
    public ResponseEntity<Void> deleteFeedback(@PathVariable Integer feedbackId) throws Exception {
        feedbackService.deleteFeedback(feedbackId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{feedbackId}")
    public ResponseEntity<FeedbackResponses> getFeedbackById(@PathVariable Integer feedbackId) throws Exception {
        FeedbackResponses feedbackResponse = feedbackService.getFeedbackById(feedbackId);
        return ResponseEntity.ok(feedbackResponse);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FeedbackResponses>> getAllFeedbacksByUserId(@PathVariable Integer userId) throws Exception {
        List<FeedbackResponses> feedbackResponses = feedbackService.getAllFeedbacksByUserId(userId);
        return ResponseEntity.ok(feedbackResponses);
    }

    @GetMapping("/item/{itemId}")
    public ResponseEntity<List<FeedbackResponses>> getAllFeedbacksByItemId(@PathVariable Integer itemId) throws Exception {
        List<FeedbackResponses> feedbackResponses = feedbackService.getAllFeedbacksSellerId(itemId);
        return ResponseEntity.ok(feedbackResponses);
    }
}