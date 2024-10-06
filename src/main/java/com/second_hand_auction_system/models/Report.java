package com.second_hand_auction_system.models;

import com.second_hand_auction_system.utils.ReportPriority;
import com.second_hand_auction_system.utils.ReportType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "report")
public class Report extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reportId;

    @Column(name = "evidence")
    private String evidence;

    @Column(name = "reason")
    private String reason;

    @Column(name = "create_By")
    private String createBy;

    @Enumerated(EnumType.STRING)
    private ReportPriority priority;

    @Enumerated(EnumType.STRING)
    private ReportType type;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
