package com.second_hand_auction_system.models;

import com.second_hand_auction_system.utils.Gender;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "kyc")
public class KnowYourCustomer extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer kycId;

    @Column(name ="date_of_birth")
    private Date dateOfBirth;

    @Column(name = "age")
    private int age;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "email")
    private String email;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "reason")
    private String reason;

    @Column(name = "document")
    private String document;

    @Column(name = "kyc_status")
    private String kycStatus;

    @Column(name = "submited")
    private LocalDateTime sumbited;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}
