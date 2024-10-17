package com.second_hand_auction_system.dtos.request.kyc;

import com.second_hand_auction_system.utils.KycStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Getter
@Setter
public class ApproveKyc {
    @NotBlank(message = "Verified By cannot be blank")
    private String verifiedBy;

    private String reason;

    @NotNull(message = "Status cannot be null")
    private KycStatus status;
}
