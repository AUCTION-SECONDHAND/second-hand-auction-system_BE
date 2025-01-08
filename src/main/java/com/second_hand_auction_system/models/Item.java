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

    @Column(name = "item_description", columnDefinition = "TEXT")
    private String itemDescription;

//    @Enumerated(EnumType.STRING)
//    private ItemCondition itemCondition;

    @Enumerated(EnumType.STRING)
    private ItemStatus itemStatus;

    @Column(name = "thumbnail")
    private String thumbnail;

    @Column(name = "price_buy_now")
    private Double priceBuyNow;

    @Column(name = "price_step_item")
    private Double priceStepItem;

    @Column(name = "item_document")
    private String itemDocument;

    @Column(name = "reason")
    private String reason;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<ImageItem> imageItems;

    @Column(name = "create_By")
    private String createBy;

    @Column(name = "update_By")
    private String updateBy;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonManagedReference
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(mappedBy = "item",cascade = CascadeType.ALL,optional = true)
    private Auction auction;

    @Column(name = "imei")
    private String imei;

    @Column(name = "storage")
    private String storage;

    @Column(name = "color")
    private String color;

    @Column(name = "battery_health")
    private Double batteryHealth;

    @Column(name = "os_version")
    private String osVersion;

    @Column(name = "icloud_status")
    private String icloudStatus;

    @Column(name = "body_condition")
    private String bodyCondition;

    @Column(name = "screen_condition")
    private String screenCondition;

    @Column(name = "camera_condition")
    private String cameraCondition;

    @Column(name = "port_condition")
    private String portCondition;

    @Column(name = "button_condition")
    private String buttonCondition;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "item_specification_id", referencedColumnName = "itemSpecificationId", nullable = false)
    private ItemSpecification itemSpecification;



    @ManyToOne
    @JoinColumn(name = "sub_category_id")
    private SubCategory subCategory;

    @ManyToOne
    @JoinColumn(name = "auction_type_id")
    private AuctionType auctionType;

    @Column(name = "brand")
    private String brand;

    @Column(name = "model")
    private String model;

    @Column(name = "serial")
    private Integer serial;

    @Column(name = "control_number")
    private Integer controlNumber;

    @Column(name = "valid")
    private Boolean valid;

    @Column(name = "manufacturer")
    private String manufacturer;

    @Column(name = "type")
    private String type; // Loại thiết bị, e.g., "Smartphone"

    @Column(name = "device_image")
    private String deviceImage; // Link ảnh từ API


}
