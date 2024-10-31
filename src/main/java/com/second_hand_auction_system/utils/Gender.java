package com.second_hand_auction_system.utils;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Gender {
    MALE,FEMALE,OTHER;

    @JsonCreator
    public static Gender fromString(String gender) {
        return Gender.valueOf(gender.toUpperCase());
    }
}
