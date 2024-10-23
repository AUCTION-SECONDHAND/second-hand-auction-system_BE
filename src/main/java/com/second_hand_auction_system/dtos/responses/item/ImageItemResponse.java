package com.second_hand_auction_system.dtos.responses.item;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ImageItemResponse {
    private Integer idImage;
    private String image;
}
