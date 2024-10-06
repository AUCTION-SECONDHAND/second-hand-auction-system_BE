package com.second_hand_auction_system.common;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebCommon {

    public static String generateCodeFromName(String name) {
        return name.replaceAll("\\s", "-").toLowerCase();
    }

}
