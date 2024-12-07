package com.second_hand_auction_system.dtos.request.kyc;

import jakarta.validation.constraints.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class KycDto {

    @NotBlank(message = "CCCD number cannot be blank")
    @Size(min = 12, max = 12, message = "CCCD number must be exactly 12 characters long")
    @Pattern(regexp = "\\d{12}", message = "CCCD number must contain only digits")
    private String cccdNumber;

    @NotBlank(message = "Full name cannot be blank")
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;

    @NotNull(message = "Date of birth cannot be null")
    @Past(message = "Date of birth must be in the past")
    private String dob;

    @NotBlank(message = "Gender cannot be blank")
    private String gender;

    @NotBlank(message = "Nationality cannot be blank")
    @Size(max = 50, message = "Nationality must not exceed 50 characters")
    private String nationality;

    @NotBlank(message = "Permanent address cannot be blank")
    @Size(max = 255, message = "Permanent address must not exceed 255 characters")
    private String permanentAddress;

    @NotBlank(message = "Home cannot be blank")
    @Size(max = 255, message = "Home must not exceed 255 characters")
    private String home;

    @NotBlank(message = "Image cannot be blank")
    private String image;

}
