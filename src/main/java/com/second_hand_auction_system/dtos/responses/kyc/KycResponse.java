package com.second_hand_auction_system.dtos.responses.kyc;

import com.second_hand_auction_system.dtos.responses.address.AddressResponse;
import com.second_hand_auction_system.utils.Gender;
import com.second_hand_auction_system.utils.KycStatus;
import lombok.*;

import java.time.LocalDate;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class KycResponse {
    private Integer kycId;
    private String dob;
    private String fullName;
    private String gender;
    private String cccdNumber;
    private String permanentAddress;
    private KycStatus kycStatus;
    private Date submitted;
    private String nationality;
    private Integer userId;
    private String reason;
    private String home;
    private String imageUrl;
    private String verified_by;


}
