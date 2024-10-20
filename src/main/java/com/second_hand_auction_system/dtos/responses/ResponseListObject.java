package com.second_hand_auction_system.dtos.responses;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseListObject<T> {
    private T data;
    private int totalPages;
    private Long totalElements;
}
