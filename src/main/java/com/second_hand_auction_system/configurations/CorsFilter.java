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

@Component("customCorsFilter")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Cho phép tất cả nguồn hoặc có thể chỉ định tên miền cụ thể thay vì "*"
        response.setHeader("Access-Control-Allow-Origin", "http://103.163.24.146:8080/*");  // Thay đổi domain nếu cần
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "authorization, content-type, xsrf-token, x-requested-with");
        response.addHeader("Access-Control-Expose-Headers", "xsrf-token");

        // Cho phép cookies, credentials nếu cần
        response.setHeader("Access-Control-Allow-Credentials", "true");

        // Nếu là yêu cầu OPTIONS, trả về thành công ngay lập tức
        if ("OPTIONS".equals(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
