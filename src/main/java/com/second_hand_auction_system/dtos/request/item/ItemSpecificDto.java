package com.second_hand_auction_system.dtos.request.item;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.second_hand_auction_system.models.Item;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.Date;
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemSpecificDto {

    // Phần trăm giảm giá hoặc thông số đặc biệt
    @Min(value = 0, message = "Percent must be greater than or equal to 0")
    @Max(value = 100, message = "Percent must be less than or equal to 100")
    @JsonProperty("percent")
    private double percent;

    // Loại sản phẩm
    @NotBlank(message = "Type is required")
    @JsonProperty("type")
    private String type;

    // Màu sắc sản phẩm
    @NotBlank(message = "Color is required")
    @JsonProperty("color")
    private String color;

    // Cân nặng sản phẩm
    @Min(value = 0, message = "Weight must be greater than or equal to 0")
    @JsonProperty("weight")
    private double weight;

    // Kích thước sản phẩm (chiều dài x chiều rộng x chiều cao)
    @NotBlank(message = "Dimension is required")
    @JsonProperty("dimension")
    private String dimension;

    // Xuất xứ sản phẩm
    @NotBlank(message = "Original is required")
    @JsonProperty("original")
    private String original;

    // Ngày sản xuất
    @NotNull(message = "Manufacture date is required")
    @Past(message = "Manufacture date must be in the past")
    @JsonProperty("manufacture_date")
    private Date manufactureDate;

    // Chất liệu sản phẩm
    @NotBlank(message = "Material is required")
    @JsonProperty("material")
    private String material;

    // Liên kết với đối tượng Item
    @JsonProperty("item")
    private Integer item;
}
