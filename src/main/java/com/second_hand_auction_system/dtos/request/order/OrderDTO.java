package com.second_hand_auction_system.dtos.request.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.second_hand_auction_system.utils.PaymentMethod;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class OrderDTO {
//    @Min(value = 0, message = "Total amount must be positive")
//    private double totalAmount;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email cannot be blank")
    private String email;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    @NotBlank(message = "Phone number cannot be blank")
    private String phoneNumber;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    private String note;

    private String createBy;

//    @NotNull(message = "Item is required")
//    private Integer item;

    private Integer auction;

    private String returnSuccess;
    private String failureUrl;


}
