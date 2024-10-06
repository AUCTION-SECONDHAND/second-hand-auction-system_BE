package com.second_hand_auction_system.dtos.request.item;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.second_hand_auction_system.models.*;
import com.second_hand_auction_system.utils.ItemStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @Size(min = 10, max = 500, message = "Item description must be between 10 and 500 characters")
    @JsonProperty("item_description")
    private String itemDescription;

    // Điều kiện sản phẩm (mới, đã qua sử dụng,...)
    @NotBlank(message = "Item condition is required")
    @JsonProperty("item_condition")
    private String itemCondition;

    @NotNull(message = "Item status is required")
    @JsonProperty("item_status")
    private ItemStatus itemStatus;

    // Tên thương hiệu
    @NotBlank(message = "Brand name is required")
    @JsonProperty("brand_name")
    private String brandName;

    // Đường dẫn ảnh thumbnail
//    @NotBlank(message = "Thumbnail is required")
//    @JsonProperty("thumbnail")
//    private String thumbnail;

    // Đường dẫn ảnh của sản phẩm
    //@NotBlank(message = "Item image is required")
    @JsonProperty("img_item")
    private List<ImgItemDto> imgItem;

    // Mã người dùng (liên kết với người dùng)
    @NotNull(message = "User ID is required")
    @JsonProperty("user_id")
    private Integer userId;

    // Đánh giá (nếu có)
//    @JsonProperty("feedback")
//    private FeedBack feedback;

    // Thông tin đấu giá (nếu có)
//    @JsonProperty("auction")
//    private Auction auction;

    // Thông tin chi tiết sản phẩm (nếu có)
    @JsonProperty("item_specific")
    private ItemSpecificDto itemSpecific;

    // Mã subcategory (phân loại sản phẩm)
    @NotNull(message = "SubCategory ID is required")
    @JsonProperty("sc_id")
    private Integer scId;
}
