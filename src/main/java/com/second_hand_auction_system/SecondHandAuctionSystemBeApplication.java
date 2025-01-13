package com.second_hand_auction_system;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.TimeZone;

@RestController
@SpringBootApplication
@EnableScheduling
@RequestMapping(path = "/")
@RequiredArgsConstructor
public class SecondHandAuctionSystemBeApplication {
    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SpringApplication.run(SecondHandAuctionSystemBeApplication.class, args);
    }

    @GetMapping("")
    public String greeting() {
        return "From auctionSecondHand with love <3 !";
    }
}
