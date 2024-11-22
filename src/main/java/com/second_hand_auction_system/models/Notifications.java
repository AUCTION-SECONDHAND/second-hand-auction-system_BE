package com.second_hand_auction_system.models;

import com.second_hand_auction_system.utils.NotificationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "notifications")
public class Notifications extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer notificationId;

    @Column(name = "title")
    private String title;

    @Column(name = "message")
    private String message;

    @Enumerated(EnumType.STRING)
    private NotificationStatus notificationStatus;

    @Column(name = "create_by")
    private String createBy;

    //    @ManyToMany
//    @JoinTable(name = "user_notification",
//               joinColumns = @JoinColumn(name = "user_id"),
//                inverseJoinColumns = @JoinColumn(name = "notification_id"))
//    private List<User> users;
    @ManyToOne
    @JoinColumn(name = "user_notification")
    private User user;

    private Boolean status;
}
