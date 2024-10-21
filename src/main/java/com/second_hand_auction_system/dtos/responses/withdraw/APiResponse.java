package com.second_hand_auction_system.dtos.responses.withdraw;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class APiResponse {
    private String message;
    private String data;
    private int code;
}
