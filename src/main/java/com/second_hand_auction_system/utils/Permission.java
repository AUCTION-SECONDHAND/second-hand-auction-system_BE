package com.second_hand_auction_system.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Permission {
    ADMIN_VIEW("admin:read"),
    ADMIN_UPDATE("admin:update"),
    ADMIN_CREATE("admin:create"),
    ADMIN_DELETE("admin:delete"),

    SELLER_VIEW("seller:read"),
    SELLER_UPDATE("seller:update"),
    SELLER_CREATE("seller:create"),
    SELLER_DELETE("seller:delete"),

    BUYER_VIEW("buyer:read"),
    BUYER_UPDATE("buyer:update"),
    BUYER_CREATE("buyer:create"),
    BUYER_DELETE("buyer:delete"),

    STAFF_VIEW("staff:read"),
    STAFF_UPDATE("staff:update"),
    STAFF_CREATE("staff:create"),
    STAFF_DELETE("staff:delete");

    private final String permission;
}
