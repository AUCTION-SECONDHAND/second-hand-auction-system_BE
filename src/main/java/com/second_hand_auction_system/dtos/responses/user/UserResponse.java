package com.second_hand_auction_system.dtos.responses.user;


import com.second_hand_auction_system.models.Wallet;
import com.second_hand_auction_system.utils.Role;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class UserResponse {
    private Integer id;
    private String fullName;
    private String email;
    private String avatar;
    private String phoneNumber;
    @Enumerated(EnumType.STRING)
    private Role role;
    private boolean status;

    private Double balance;

}
