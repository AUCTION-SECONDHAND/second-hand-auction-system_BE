package com.second_hand_auction_system.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "feedback")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class FeedBack extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer feedbackId;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "rating")
    private int rating;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne
    @JoinColumn(name = "item_id")
    private Item item;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "reply_comment", columnDefinition = "TEXT")
    private String replyComment;

    @Column(name = "replied")
    private boolean replied;

}
