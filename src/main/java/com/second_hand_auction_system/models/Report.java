package com.second_hand_auction_system.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.second_hand_auction_system.utils.ReportPriority;
import com.second_hand_auction_system.utils.ReportStatus;
import com.second_hand_auction_system.utils.ReportType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Builder
@Table(name = "report")
public class Report extends BaseEntity {
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

    @Enumerated(EnumType.STRING)
    private ReportStatus status;

    @Column(name = "processed_by")
    private String processedBy;

    @Column(name = "response_message")
    private String responseMessage;

    @Column(name = "response_create_time")
    @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
    private LocalDateTime responseCreateTime;

    @Column(name = "response_update_time")
    @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
    private LocalDateTime responseUpdateTime;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id")
    private Order order;

    private String ticketId;

}
