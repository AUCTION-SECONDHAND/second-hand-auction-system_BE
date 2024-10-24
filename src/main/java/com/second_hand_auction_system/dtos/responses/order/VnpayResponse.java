package com.second_hand_auction_system.dtos.responses.order;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder

public class VnpayResponse {
    private String transactionId; // ID giao dịch từ VNPay
    private String orderId; // ID đơn hàng của bạn
    private String amount; // Số tiền giao dịch
    private String responseCode; // Mã phản hồi từ VNPay
    private String message; // Thông điệp từ VNPay (nếu có)
    private String paymentUrl; // URL thanh toán (nếu cần)
    private LocalDateTime transactionDate; // Ngày giờ giao dịch
    private String bankCode;
}
