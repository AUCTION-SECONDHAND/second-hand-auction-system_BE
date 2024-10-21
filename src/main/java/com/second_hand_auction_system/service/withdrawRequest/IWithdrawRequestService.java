package com.second_hand_auction_system.service.withdrawRequest;

import com.second_hand_auction_system.dtos.request.withdrawRequest.WithdrawApprove;
import com.second_hand_auction_system.dtos.request.withdrawRequest.WithdrawRequestDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

public interface IWithdrawRequestService {
    ResponseEntity<?> requestWithdraw(WithdrawRequestDTO withdrawRequest);

    ResponseEntity<?> approve(Integer id, WithdrawApprove withdrawApprove, HttpServletRequest request);
}
