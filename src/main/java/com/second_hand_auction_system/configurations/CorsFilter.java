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
        // Chỉ định tên miền cụ thể thay vì "*"
        response.setHeader("Access-Control-Allow-Origin", "*");  // Sửa lại thành tên miền của bạn

        // Cấu hình các phương thức được phép
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
