package com.second_hand_auction_system.dtos.request.walletCustomer;

import com.second_hand_auction_system.utils.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Deposit {
    @NotNull(message = "Payment method cannot be null")
    private PaymentMethod paymentMethod;

    @NotNull(message = "BankName cannot be null")
    private String bankName;

    @NotNull(message = "Description cannot be null")
    private String description;

    @Positive(message = "Amount must be a positive number")
    private int amount;

    @NotBlank(message = "Return success URL cannot be blank")
    private String returnSuccess;

    @NotBlank(message = "Return error URL cannot be blank")
    private String returnError;


}
