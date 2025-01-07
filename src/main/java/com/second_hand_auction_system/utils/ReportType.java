package com.second_hand_auction_system.utils;

public enum ReportType {
    //Hàng lỗi
    DAMAGED_PRODUCT,
    //Không nhận được tiền
    MISSING_BALANCE,
    //Dịch vụ không hoạt động
    SERVICE_NOT_WORKING,
    //Lỗi giao dịch
    TRANSACTION_ERROR,
    //Tài khoản bị khóa
    ACCOUNT_LOCKED,
    //Lỗi hiển thị
    DISPLAY_ERROR,
    //Lỗi khác
    OTHER
}
