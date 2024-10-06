package com.second_hand_auction_system.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.second_hand_auction_system.utils.ItemStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "item")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Item extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer itemId;

    @Column(name = "title")
    private String title;

    @Column(name = "item_name")
    private String itemName;

    @Column(name = "item_description")
    private String itemDescription;

    @Column(name = "item_condition")
    private String itemCondition;

    @Enumerated(EnumType.STRING)
    private ItemStatus itemStatus;

    @Column(name = "brand_name")
    private String brandName;

//    @Lob
//    @Column(name = "document")
//    private byte[] fileData;

    @Column(name = "thumbnail")
    private String thumbnail;

    @Column(name = "img_default")
    private String imgDefault;

//    @Column(name = "img_item")
//    private String imgItem;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<ImageItem> imageItems;

    @Column(name = "create_By")
    private String createBy;

    @Column(name = "update_By")
    private String updateBy;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(optional = true)
    @JoinColumn(name = "feedback_id")
    private FeedBack feedback;

//    @OneToOne(mappedBy = "item",cascade = CascadeType.ALL,optional = true)
//    private Auction auction;

    @OneToOne(mappedBy = "item",cascade = CascadeType.ALL)
    private ItemSpecific itemSpecific;

    @ManyToOne
    @JoinColumn(name = "sub_category_id")
    private SubCategory subCategory;
}
