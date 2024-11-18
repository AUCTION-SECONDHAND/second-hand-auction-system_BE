package com.second_hand_auction_system.utils;

public enum OrderStatus {
    ready_to_pick,
    picking,
    money_collect_picking,
    picked,
    storing,
    transporting,
    sorting,
    delivering,
    //delivered successfully
    delivered,
    money_collect_delivering,
    delivery_fail,
    waiting_to_return,
    return_transporting,
    return_sorting,
    returning,
    return_fail,
    returned,
    cancel,
    exception,
    lost,
    damage

}
