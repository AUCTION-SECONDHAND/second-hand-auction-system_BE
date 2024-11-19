package com.second_hand_auction_system.dtos.responses.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Data
public class GHNResponse {
    private int code;
    private String message;
    private GHNData data;

    @Data
    public static class GHNData {
        private String order_code;
        private String status;
        private String to_name;
        private String to_phone;
        private String to_address;
        private List<GHNItem> items;

        @Data
        public static class GHNItem {
            private String name;
            private int quantity;
        }
    }
}
