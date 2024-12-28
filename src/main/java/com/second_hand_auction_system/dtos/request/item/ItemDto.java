package com.second_hand_auction_system.dtos.request.item;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.second_hand_auction_system.models.*;
import com.second_hand_auction_system.utils.ItemCondition;
import com.second_hand_auction_system.utils.ItemStatus;
import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemDto {
    //private Integer itemId;

    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 200, message = "Product name must be between 3 and 200 characters")
    @JsonProperty("item_name")
    private String itemName;

    @NotBlank(message = "Item description is required")
    @JsonProperty("item_description")
    private String itemDescription;

//    @NotNull(message = "Item condition is required")
//    @JsonProperty("item_condition")
//    private ItemCondition itemCondition;

    @JsonProperty("price_step_item")
    @NotNull(message = "Price step item cannot be null")
    @DecimalMin(value = "0.01", message = "Price step item must be greater than 0")
    private Double priceStepItem;

    @NotNull(message = "Giá mua ngay không được để trống.")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be greater than or equal to 0")
    @JsonProperty("price_buy_now")
    private Double priceBuyNow;

    @JsonProperty("item_document")
    private String itemDocument;

    @JsonProperty("imei")
    @Size(min = 15, max = 15, message = "IMEI must be 15 digits long")  // Kiểm tra độ dài IMEI
    private String imei;

    @JsonProperty("storage")
    private String storage;

    @JsonProperty("color")
    private String color;

    @JsonProperty("battery_health")
    private Double batteryHealth;

    @JsonProperty("os_version")
    private String osVersion;

    @JsonProperty("icloud_status")
    private String icloudStatus;

    @JsonProperty("body_condition")
    private String bodyCondition;

    @JsonProperty("screen_condition")
    private String screenCondition;

    @JsonProperty("camera_condition")
    private String cameraCondition;

    @JsonProperty("port_condition")
    private String portCondition;

    @JsonProperty("button_condition")
    private String buttonCondition;

    @JsonProperty("cpu")
    private String cpu;

    @JsonProperty("ram")
    private String ram;

    @JsonProperty("screen_size")
    private String screenSize;

    @JsonProperty("camera_specs")
    private String cameraSpecs;

    @JsonProperty("connectivity")
    private String connectivity;

    @JsonProperty("sensors")
    private String sensors;

    @JsonProperty("img_item")
    @NotEmpty(message = "At least one image is required")  // Kiểm tra ảnh nếu cần
    private List<ImgItemDto> imgItem;

    @NotNull(message = "SubCategory ID is required")
    @JsonProperty("sc_id")
    private Integer scId;

//    @JsonProperty("item_spec_id")
//    private Integer itemSpecificationId;

    @NotNull(message = "AuctionType ID is required")
    @JsonProperty("auction_type")
    private Integer auctionType;
}
