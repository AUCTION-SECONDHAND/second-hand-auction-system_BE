package com.second_hand_auction_system.dtos.request.kyc;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class KycDto {

    private String cccdNumber;
    private String fullName;
    private String dob;
    private String gender;
    private String nationality;
    private String permanentAddress;
    private String home;
    private String image;

}
