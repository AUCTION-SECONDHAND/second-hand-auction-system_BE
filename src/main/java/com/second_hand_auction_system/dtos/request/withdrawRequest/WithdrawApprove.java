package com.second_hand_auction_system.dtos.request.withdrawRequest;

import com.second_hand_auction_system.utils.RequestStatus;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Setter
@Getter
public class WithdrawApprove {
    private RequestStatus status;
}
