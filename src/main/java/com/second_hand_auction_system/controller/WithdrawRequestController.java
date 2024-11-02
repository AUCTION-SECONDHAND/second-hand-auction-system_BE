package com.second_hand_auction_system.controller;

import com.second_hand_auction_system.dtos.request.withdrawRequest.WithdrawApprove;
import com.second_hand_auction_system.dtos.request.withdrawRequest.WithdrawRequestDTO;
import com.second_hand_auction_system.dtos.responses.withdraw.APiResponse;
import com.second_hand_auction_system.service.VNPay.VNPAYService;
import com.second_hand_auction_system.service.withdrawRequest.IWithdrawRequestService;
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
//    private final TransactionSystemRepository transactionSystemRepository;

    @PostMapping("")
    public ResponseEntity<?> withdraw(@RequestBody WithdrawRequestDTO withdrawRequest) {
        return  withdrawRequestService.requestWithdraw(withdrawRequest);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> approveRequest(@PathVariable("id") Integer id, @RequestBody WithdrawApprove withdrawApprove, HttpServletRequest request) {
        return withdrawRequestService.approve(id,withdrawApprove,request);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> submitOrder(@RequestParam("amount") int orderTotal,@RequestParam("withdrawInfo") int withdraw, HttpServletRequest request) {
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
                                       @RequestParam(name = "vnp_SecureHash") String vnpSecureHash
//                                       @RequestParam(name = "id") Integer transactionId
    ) {
        log.info("vnpAmount|{}|vnpBankCode|{}|vnpBankTranNo|{}|vnpCardType|{}|vnpOrderInfo|{}|vnpPayDate|{}|" +
                        "vnpPayDate|{}|vnpTmnCode|{}|vnpTransactionNo|{}|vnpTransactionStatus|{}|vnpTxnRef|{}|vnpSecureHash|{}",
                vnpAmount, vnpBankCode, vnpBankTranNo, vnpCardType, vnpOrderInfo, vnpPayDate, vnpPayDate, vnpTmnCode,
                vnpTransactionNo, vnpTransactionStatus, vnpTxnRef, vnpSecureHash);
//        var transactionType = transactionSystemRepository.findById(transactionId).orElseThrow(null);

        APiResponse response = new APiResponse();
        if(vnpTransactionStatus.equals("00")) {
            response.setCode("200");
            response.setMessage("Payment success");
//            transactionType.setStatus(TransactionStatus.COMPLETED);
//            transactionSystemRepository.save(transactionType);
        } else {
            response.setCode("500");
            response.setMessage("Payment processing error");
//            transactionType.setStatus(TransactionStatus.FAILED);
//            transactionSystemRepository.save(transactionType);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
