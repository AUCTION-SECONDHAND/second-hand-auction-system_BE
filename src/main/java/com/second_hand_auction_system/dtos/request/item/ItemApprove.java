package com.second_hand_auction_system.dtos.request.item;

import com.second_hand_auction_system.utils.ItemStatus;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ItemApprove {
    private ItemStatus status;
}
