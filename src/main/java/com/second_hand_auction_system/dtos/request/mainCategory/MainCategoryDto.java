package com.second_hand_auction_system.dtos.request.mainCategory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.second_hand_auction_system.models.Item;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MainCategoryDto {
    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 200, message = "SubCategory must be between 3 and 200 characters")
    @JsonProperty("category_name")
    private String categoryName;

    @JsonProperty("description")
    private String description;

    @JsonProperty("icon_url")
    private String iconUrl;

}
