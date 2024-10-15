package com.second_hand_auction_system.service.knowYourCustomer;

import com.second_hand_auction_system.dtos.request.kyc.KycDto;
import org.springframework.http.ResponseEntity;

public interface IKnowYourCustomerService {
    ResponseEntity<?> register(KycDto kyc);
}
