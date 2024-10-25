package com.second_hand_auction_system.service.transactionSystem;

import com.second_hand_auction_system.dtos.responses.ResponseObject;
import com.second_hand_auction_system.repositories.TransactionSystemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionSystemService implements ITransactionSystemSerivce{
    private final TransactionSystemRepository transactionSystemRepository;
    @Override
    public ResponseEntity<?> get(int id) {
        var transactionSystem = transactionSystemRepository.findById(id);
        if (transactionSystem.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder().status(HttpStatus.NOT_FOUND).message("Not found").data(null).build());

        }
        return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder().status(HttpStatus.OK).message("Found").data(transactionSystem).build());
    }
}
