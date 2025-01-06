package com.second_hand_auction_system.configurations;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component("customCorsFilter")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsFilter extends OncePerRequestFilter {
    private static final List<String> ALLOWED_ORIGINS = Arrays.asList(
            "https://auction-system-plum.vercel.app",
            "http://localhost:5174"
    );
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//        response.setHeader("Access-Control-Allow-Origin", "https://auction-system-plum.vercel.app");  // Sửa lại thành tên miền của bạn
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String origin = ((HttpServletRequest) request).getHeader("Origin");

        if (ALLOWED_ORIGINS.contains(origin)) {
            httpResponse.setHeader("Access-Control-Allow-Origin", origin);
        }
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH, OPTIONS");

        // Các header được phép trong yêu cầu
        response.setHeader("Access-Control-Allow-Headers", "authorization, content-type, xsrf-token, x-requested-with");

        // Expose header nếu cần
        response.addHeader("Access-Control-Expose-Headers", "xsrf-token");

        // Cho phép cookies và credentials
        response.setHeader("Access-Control-Allow-Credentials", "true");

        // Nếu yêu cầu là OPTIONS, trả về thành công
        if ("OPTIONS".equals(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
