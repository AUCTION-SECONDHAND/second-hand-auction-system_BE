package com.second_hand_auction_system.dtos.responses.item;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemSpecificationResponse {
    private String cpu;
    private String ram;
    private String screenSize;
    private String cameraSpecs;
    private String connectivity;
    private String sensors;
}
