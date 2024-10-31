package com.second_hand_auction_system.dtos.responses.kyc;

import com.second_hand_auction_system.dtos.responses.address.AddressResponse;
import com.second_hand_auction_system.utils.Gender;
import com.second_hand_auction_system.utils.KycStatus;
import lombok.*;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class KycResponse {
    private Integer kycId;
    private String dob;
    private int age;
    private String fullName;
    private String phoneNumber;
    private String email;
    private Gender gender;
    private String cccdNumber;
    private String frontDocumentUrl;
    private String backDocumentUrl;
    private KycStatus kycStatus;
    private Date submited;
    private Integer userId;
    private AddressResponse address;
    private String reason;
    private String verified_by;


}
