package com.second_hand_auction_system.utils;

public enum ItemCondition {
    AVAILABLE,       // Sẵn có để bán hoặc đấu giá
    NEW,               // Mới, chưa qua sử dụng
    LIKE_NEW,          // Như mới, đã sử dụng nhưng trong tình trạng rất tốt
    USED_GOOD,         // Đã qua sử dụng, tình trạng tốt
    USED_FAIR,         // Đã qua sử dụng, tình trạng chấp nhận được
    REFURBISHED,       // Đã được tân trang, sửa chữa
    DAMAGED,           // Bị hư hỏng, không hoàn hảo   // Bị hư hỏng
}
