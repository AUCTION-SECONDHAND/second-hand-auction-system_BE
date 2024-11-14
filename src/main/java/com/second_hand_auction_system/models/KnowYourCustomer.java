package com.second_hand_auction_system.models;

import com.second_hand_auction_system.utils.Gender;
import com.second_hand_auction_system.utils.KycStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
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
    private LocalDate dateOfBirth;

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

    @Column(name = "cccd_number")
    private String cccdNumber;

    @Column(name = "front_document")
    private String frontDocumentUrl;

    @Column(name = "back_document")
    private String backDocumentUrl;

    @Enumerated(EnumType.STRING)
    private KycStatus kycStatus;

    @Column(name = "submitted")
    private Date submitted;

    @Column(name = "verified_by")
    private String verifiedBy;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}
