package com.second_hand_auction_system.dtos.request.user;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
@Builder
public class ChangePassWordDTO {
    private String password;
    private String newPassword;
    private String confirmPassword;
}
