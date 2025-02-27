package com.second_hand_auction_system.service.walletCustomer;

import com.second_hand_auction_system.dtos.request.walletCustomer.Deposit;
import com.second_hand_auction_system.dtos.responses.ResponseObject;
import org.springframework.http.ResponseEntity;

public interface IWalletService {
    ResponseEntity<ResponseObject> depositWallet(Deposit deposit);

//    ResponseEntity<ResponseObject> getWalletCustomer(Long id);

    ResponseEntity<ResponseObject> getWalletCustomerBalance();

//    ResponseEntity<?> updateStatus(PaymentRequest payment);
}
