package com.second_hand_auction_system.dtos.request.kyc;

import com.second_hand_auction_system.utils.Gender;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class KycDto {
    @Past(message = "Date of birth must be a past date")
    @NotNull(message = "Date of birth not blank")
    private LocalDate dob;
    private Gender gender;

    @NotBlank(message = "Full name cannot be blank")
    private String fullName;

    @Min(value = 0, message = "Age must be a positive number")
    private int age;

    @NotBlank(message = "Phone number cannot be blank")
    @Pattern(regexp = "^\\+?[0-9. ()-]{7,25}$", message = "Invalid phone number format")
    private String phoneNumber;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    private String email;

    @NotNull(message = "CCCD cannot be null")
    @Size(min = 12, max = 12)
    private String cccdNumber;

    private String frontDocumentUrl;

    private String backDocumentUrl;
}
