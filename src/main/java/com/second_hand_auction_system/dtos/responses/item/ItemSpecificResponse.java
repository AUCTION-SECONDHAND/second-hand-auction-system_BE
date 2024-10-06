package com.second_hand_auction_system.dtos.responses.item;

import com.second_hand_auction_system.models.Item;

import java.util.Date;

public class ItemSpecificResponse {

    // Phần trăm giảm giá hoặc thông số đặc biệt
    private double percent;

    // Loại sản phẩm
    private String type;

    // Màu sắc sản phẩm
    private String color;

    // Cân nặng sản phẩm
    private double weight;

    // Kích thước sản phẩm (chiều dài x chiều rộng x chiều cao)
    private String dimension;

    // Xuất xứ sản phẩm
    private String original;

    // Ngày sản xuất
    private Date manufactureDate;

    // Chất liệu sản phẩm
    private String material;

    // Liên kết với đối tượng Item
    private Item item;
}
