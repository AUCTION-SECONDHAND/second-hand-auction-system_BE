package com.second_hand_auction_system.service.walletCustomer;

import com.second_hand_auction_system.dtos.request.walletCustomer.Deposit;
import com.second_hand_auction_system.dtos.request.walletCustomer.PaymentRequest;
import com.second_hand_auction_system.dtos.responses.ResponseObject;
import org.springframework.http.ResponseEntity;

public interface IWalletCustomerService {
    ResponseEntity<ResponseObject> depositWallet(Deposit deposit);

    ResponseEntity<ResponseObject> getWalletCustomer(Long id);

    ResponseEntity<?> updateStatus(PaymentRequest payment);
}
