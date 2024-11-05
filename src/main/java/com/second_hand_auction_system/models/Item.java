package com.second_hand_auction_system.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.second_hand_auction_system.utils.ItemCondition;
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

    @Column(name = "item_name")
    private String itemName;

    @Column(name = "item_description")
    private String itemDescription;

    @Enumerated(EnumType.STRING)
    private ItemCondition itemCondition;

    @Enumerated(EnumType.STRING)
    private ItemStatus itemStatus;

    @Column(name = "brand_name")
    private String brandName;

    @Column(name = "thumbnail")
    private String thumbnail;

//    @Column(name = "img_default")
//    private String imgDefault;

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

    @OneToOne(mappedBy = "item",cascade = CascadeType.ALL,optional = true)
    private Auction auction;

    @OneToOne(mappedBy = "item",cascade = CascadeType.ALL)
    private ItemSpecific itemSpecific;

    @ManyToOne
    @JoinColumn(name = "sub_category_id")
    private SubCategory subCategory;

    @ManyToOne
    @JoinColumn(name = "auction_type_id")
    private AuctionType auctionType;
}
