package com.second_hand_auction_system.models;

import com.second_hand_auction_system.utils.Gender;
import com.second_hand_auction_system.utils.KycStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Date;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "kyc")
public class KnowYourCustomer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer kycId;

    @Column(name = "date_of_birth")
    private String dateOfBirth;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "gender")
    private String gender;

    @Column(name = "reason")
    private String reason;

    @Column(name = "cccd_number")
    private String cccdNumber;

    @Column(name = "nationality")
    private String nationality;

    @Enumerated(EnumType.STRING)
    private KycStatus kycStatus;

    @Column(name = "submitted")
    private Date submitted;

    @Column(name = "verified_by")
    private String verifiedBy;

    @Column(name = "permanent_address")
    private String permanentAddress;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;



    @Column(name = "home")
    private String home;
}
