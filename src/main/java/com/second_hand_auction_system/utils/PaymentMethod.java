package com.second_hand_auction_system.utils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PaymentMethod {
    VN_PAYMENT("VNPAY_PAYMENT"),
    WALLET_PAYMENT("WALLET_PAYMENT");

    private final String value;

    PaymentMethod(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static PaymentMethod fromString(String value) {
        for (PaymentMethod method : PaymentMethod.values()) {
            if (method.value.equals(value)) {
                return method;
            }
        }
        throw new IllegalArgumentException("Unknown value: " + value);
    }
}
