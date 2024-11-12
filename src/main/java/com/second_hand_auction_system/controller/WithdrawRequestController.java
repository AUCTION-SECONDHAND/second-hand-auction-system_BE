package com.second_hand_auction_system.controller;

import com.second_hand_auction_system.dtos.request.withdrawRequest.WithdrawApprove;
import com.second_hand_auction_system.dtos.request.withdrawRequest.WithdrawRequestDTO;
import com.second_hand_auction_system.dtos.responses.withdraw.APiResponse;
import com.second_hand_auction_system.models.Transaction;
import com.second_hand_auction_system.repositories.TransactionRepository;
import com.second_hand_auction_system.service.VNPay.VNPAYService;
import com.second_hand_auction_system.service.transactionSystem.ITransactionSystemSerivce;
import com.second_hand_auction_system.service.transactionWallet.ITransactionWalletService;
import com.second_hand_auction_system.service.withdrawRequest.IWithdrawRequestService;
import com.second_hand_auction_system.utils.TransactionStatus;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/withdrawRequest")
@RequiredArgsConstructor
@Slf4j
public class WithdrawRequestController {
    private final IWithdrawRequestService withdrawRequestService;
    private final VNPAYService vnpayService;
    private final ITransactionWalletService transactionWalletService;

    @PostMapping("")
    public ResponseEntity<?> withdraw(@RequestBody WithdrawRequestDTO withdrawRequest) {
        return withdrawRequestService.requestWithdraw(withdrawRequest);
    }

    @GetMapping
    public ResponseEntity<?> getAllWithdraw(@RequestParam(value = "page", defaultValue = "0") int page,
                                              @RequestParam(value = "limit", defaultValue = "10") int limit) throws Exception {
        return  withdrawRequestService.getAll(page, limit);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> approveRequest(@PathVariable("id") Integer id, @RequestBody WithdrawApprove withdrawApprove, HttpServletRequest request) {
        return withdrawRequestService.approve(id, withdrawApprove, request);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> submitOrder(@RequestParam("amount") int orderTotal, @RequestParam("withdrawInfo") int withdraw, HttpServletRequest request) {
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        return vnpayService.createOrder(orderTotal, withdraw, baseUrl);
    }


    @GetMapping("/results")
    public ResponseEntity<?> getResult(@RequestParam(name = "vnp_Amount") String vnpAmount,
                                       @RequestParam(name = "vnp_BankCode") String vnpBankCode,
                                       @RequestParam(name = "vnp_BankTranNo") String vnpBankTranNo,
                                       @RequestParam(name = "vnp_CardType") String vnpCardType,
                                       @RequestParam(name = "vnp_OrderInfo") String vnpOrderInfo,
                                       @RequestParam(name = "vnp_PayDate") String vnpPayDate,
                                       @RequestParam(name = "vnp_ResponseCode") String vnpResponseCode,
                                       @RequestParam(name = "vnp_TmnCode") String vnpTmnCode,
                                       @RequestParam(name = "vnp_TransactionNo") String vnpTransactionNo,
                                       @RequestParam(name = "vnp_TransactionStatus") String vnpTransactionStatus,
                                       @RequestParam(name = "vnp_TxnRef") String vnpTxnRef,
                                       @RequestParam(name = "vnp_SecureHash") String vnpSecureHash,
                                       @RequestParam(name = "transactionId") Integer transactionId
    ) {
        log.info("vnpAmount|{}|vnpBankCode|{}|vnpBankTranNo|{}|vnpCardType|{}|vnpOrderInfo|{}|vnpPayDate|{}|" +
                        "vnpPayDate|{}|vnpTmnCode|{}|vnpTransactionNo|{}|vnpTransactionStatus|{}|vnpTxnRef|{}|vnpSecureHash|{}",
                vnpAmount, vnpBankCode, vnpBankTranNo, vnpCardType, vnpOrderInfo, vnpPayDate, vnpPayDate, vnpTmnCode,
                vnpTransactionNo, vnpTransactionStatus, vnpTxnRef, vnpSecureHash);
        return transactionWalletService.updateTransaction(transactionId, vnpTransactionStatus);

    }
}
