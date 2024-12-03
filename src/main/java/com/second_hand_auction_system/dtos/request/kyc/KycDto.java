package com.second_hand_auction_system.dtos.request.kyc;

import com.second_hand_auction_system.utils.Gender;
import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

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
    private String frontDocumentUrl;

}
