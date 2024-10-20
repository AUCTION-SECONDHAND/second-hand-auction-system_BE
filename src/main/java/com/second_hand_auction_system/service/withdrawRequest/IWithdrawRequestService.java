package com.second_hand_auction_system.service.withdrawRequest;

import com.second_hand_auction_system.dtos.request.withdrawRequest.WithdrawRequestDTO;
import com.second_hand_auction_system.models.WithdrawRequest;
import org.springframework.http.ResponseEntity;

public interface IWithdrawRequestService {
    ResponseEntity<?> requestWithdraw(WithdrawRequestDTO withdrawRequest);
}
