    package com.second_hand_auction_system.dtos.responses.auctionRegistrations;

    import com.fasterxml.jackson.annotation.JsonProperty;
    import com.second_hand_auction_system.dtos.responses.BaseResponse;
    import com.second_hand_auction_system.dtos.responses.item.AuctionItemResponse;
    import com.second_hand_auction_system.utils.Registration;
    import lombok.*;

    import java.time.LocalDateTime;

    @Data
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public class AuctionRegistrationsResponse extends BaseResponse {
        @JsonProperty("ar_id")
        private Integer auctionRegistrationId;

        @JsonProperty("deposite_amount")
        private double depositeAmount;

        @JsonProperty("registration")
        private Boolean registration;

    //    @JsonProperty("note")
    //    private String note;

        private AuctionItemResponse auctionItem;

        @JsonProperty("created_date")  // Ensure the field name is correctly declared
        private LocalDateTime createdDate;

        @JsonProperty("user_name")
        private String userName;
    }
