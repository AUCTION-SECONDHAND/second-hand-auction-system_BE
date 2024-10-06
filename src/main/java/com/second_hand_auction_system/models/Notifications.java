package com.second_hand_auction_system.models;

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
public class Notifications extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer notificationId;

    @Column(name = "message")
    private String message;

    @Column(name = "create_by")
    private String createBy;

    @ManyToMany
    @JoinTable(name = "user_notification",
               joinColumns = @JoinColumn(name = "user_id"),
                inverseJoinColumns = @JoinColumn(name = "notification_id"))
    private List<User> users;
}
