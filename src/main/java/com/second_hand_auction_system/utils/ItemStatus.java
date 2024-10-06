package com.second_hand_auction_system.utils;

public enum ItemStatus {
    AVAILABLE,       // Sẵn có để bán hoặc đấu giá
    ON_AUCTION,      // Đang được đấu giá
    SOLD,            // Đã bán thành công
    OUT_OF_STOCK,    // Hết hàng
    RESERVED,        // Được giữ chỗ
    RETURNED,        // Đã trả lại
    DISCONTINUED,    // Ngừng kinh doanh
    DAMAGED,         // Bị hư hỏng
    INACTIVE         // Không hoạt động trên hệ thống
}
