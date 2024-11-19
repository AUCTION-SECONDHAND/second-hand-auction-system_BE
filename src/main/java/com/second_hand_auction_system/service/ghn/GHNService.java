package com.second_hand_auction_system.service.ghn;

import com.second_hand_auction_system.dtos.responses.order.GHNResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@Service
@RequiredArgsConstructor
public class GHNService {
    private static final String GHN_URL = "https://dev-online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/detail";

    public GHNResponse getOrderDetails(String orderCode) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Token", "a4cf5c26-7379-11ef-8e53-0a00184fe694");

        String requestBody = "{\"order_code\":\"" + orderCode + "\"}";
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<GHNResponse> response = restTemplate.exchange(
                GHN_URL,
                HttpMethod.POST,
                request,
                GHNResponse.class
        );

        return response.getBody(); // Returns the response from GHN API
    }
}
