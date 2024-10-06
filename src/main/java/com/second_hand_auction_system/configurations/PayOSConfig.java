package com.second_hand_auction_system.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.payos.PayOS;

@Configuration
public class PayOSConfig {
    @Value("${payos.api.secret}")
    private String secretKey;

    @Value("${payos.api.key}")
    private String key;

    @Value("${payos.api.check_sum}")
    private String checkSum;

    @Bean
    public PayOS payOS() {
        return new PayOS(secretKey, key, checkSum);
    }
}
