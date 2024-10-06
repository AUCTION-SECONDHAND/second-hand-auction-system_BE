package com.second_hand_auction_system.dtos.request.item;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.second_hand_auction_system.models.Item;
import jakarta.persistence.*;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ImgItemDto {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Integer imageItemId;

    @JsonProperty("image_url")
    private String imageUrl;

//    @JsonProperty("item_id")
//    private Integer item;
}
