package com.second_hand_auction_system.service.transactionWallet;

import com.second_hand_auction_system.dtos.responses.ResponseObject;
import com.second_hand_auction_system.dtos.responses.transactionWallet.ListTransactionWalletResponse;
import com.second_hand_auction_system.dtos.responses.transactionWallet.TransactionWalletResponse;
import com.second_hand_auction_system.models.TransactionWallet;
import com.second_hand_auction_system.repositories.TransactionWalletRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionWalletService implements ITransactionWalletService {
    private final TransactionWalletRepository transactionWalletRepository;
    private final ModelMapper modelMapper;
    @Override
    public ResponseEntity<?> getAll(int size, int page) {
        Pageable pageable =PageRequest.of(page, size);
        Page<TransactionWallet> transactionWallets = transactionWalletRepository.findAll(pageable);
        if (transactionWallets.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        List<TransactionWalletResponse> transactionWalletResponses = transactionWallets.getContent()
                .stream()
                .map(wallet -> modelMapper.map(wallet, TransactionWalletResponse.class))
                .toList();

        ListTransactionWalletResponse response = ListTransactionWalletResponse.builder()
                .transactionWallet(transactionWalletResponses)
                .build();

        return ResponseEntity.ok().body(ResponseObject.builder()
                .status(HttpStatus.OK)
                .message("List of transaction wallets")
                .data(transactionWallets)
                .build());
    }
}
