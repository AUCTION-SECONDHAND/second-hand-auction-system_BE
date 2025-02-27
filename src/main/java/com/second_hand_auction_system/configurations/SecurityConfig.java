package com.second_hand_auction_system.configurations;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

import static org.springframework.http.HttpMethod.*;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationProvider authenticationProvider;
    private final JwtFilter jwtFilter;
    private final LoginGoogleSuccess loginGoogleSuccess;
    private final LoginGoogleFailure loginGoogleFailure;
    private final LogoutHandler logoutHandler;
    private static final String[] WHITE_LIST = {
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/api/v1/auth/**",
            "/api/v1/users/**",
            "/api/v1/user/forgot-password/**",
            "/api/v1/withdrawRequest/vnpay-payment/**",
            "/api/v1/bids/highest-bid/**",
            "/api/v1/bids/**",
            "/api/v1/auctions/update/**",
            "/socket/**",
            "/**"
    };


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                                .requestMatchers(WHITE_LIST).permitAll()
                                .requestMatchers(GET, "/api/v1/user/comparison").hasRole("ADMIN")
                                .requestMatchers(POST, "/api/v1/user/**").hasRole("ADMIN")
                                .requestMatchers(PUT, "/api/v1/user/**").hasAnyRole("SELLER", "BUYER")
                                .requestMatchers(PATCH, "/api/v1/user/**").hasAnyRole("SELLER", "BUYER")
                                .requestMatchers(DELETE, "/api/v1/user/**").hasRole("ADMIN")
                                .requestMatchers("/api/v1/feedback/**").permitAll()
                                .requestMatchers("/api/v1/seller-information/**").permitAll()

                                //main-category
                                .requestMatchers(POST, "/api/v1/main-category/**").hasAnyRole("ADMIN", "STAFF")
                                .requestMatchers("/api/v1/main-category/**").permitAll()
                                //sub-category
                                .requestMatchers(POST, "/api/v1/sub-category/**").hasAnyRole("ADMIN", "STAFF")
                                .requestMatchers("/api/v1/sub-category/**").permitAll()
                                //wallet-customer
                                .requestMatchers(POST, "/api/v1/walletCustomer/**").hasAnyRole("BUYER", "SELLER")
                                .requestMatchers(POST, "api/v1/walletCustomer/confirm-webhook").hasAnyRole("BUYER", "SELLER")
                                .requestMatchers(GET, "/api/v1/walletCustomer/**").hasAnyRole("BUYER")
                                //item
                                .requestMatchers(POST, "/api/v1/item/**").hasRole("SELLER")
//                        .requestMatchers(PUT, "/api/v1/item/**").hasAnyRole("SELLER","ADMIN","STAFF")
                                .requestMatchers(GET, "/api/v1/item/**").permitAll()

                                //register_auction_bider
                                .requestMatchers(POST, "/api/v1/auction-register/**").hasRole("BUYER")
                                .requestMatchers(GET, "/api/v1/auction-register/**").permitAll()
                                //kyc
                                .requestMatchers(POST, "/api/v1/kyc/**").permitAll()
                                .requestMatchers(PUT, "/api/v1/kyc/user").permitAll()
                                .requestMatchers(PUT, "/api/v1/kyc/approve/**").hasAnyRole("STAFF", "ADMIN")
                                .requestMatchers(GET, "/api/v1/kyc/**").permitAll()

                                ///transactionWallet
                                .requestMatchers(GET, "/api/v1/transactionWallet/get-transaction-wallet").permitAll()
                                .requestMatchers(GET, "/api/v1/transactionWallet/get-transaction-admin").hasRole("ADMIN")
                                .requestMatchers(PUT, "/api/v1/transactionWallet/upload-evidence/**").permitAll()
                                //auction
                                .requestMatchers(POST, "/api/v1/auction-register/**").hasRole("BUYER")
                                .requestMatchers(PUT, "/api/v1/auctions/update").permitAll()
                                .requestMatchers(GET, "/api/v1/auctions/**").hasAnyRole("BUYER")
                                .requestMatchers(POST, "api/v1/auction/**").hasRole("STAFF")


                                //withdraw
                                .requestMatchers(PUT, "/api/v1/withdrawRequest/**").hasRole("STAFF")
                                .requestMatchers(GET, "api/v1/withdrawRequest/**").permitAll()
                                //auction_type
                                .requestMatchers("/api/v1/auctionType/**").hasAnyRole("ADMIN", "STAFF", "SELLER")
                                //order
                                .requestMatchers(POST, "/api/v1/orders/**").hasAnyRole("SELLER", "BUYER")
                                .requestMatchers(GET, "api/v1/orders/getTotalMoneyByMonth").hasAnyRole("SELLER")
                                .requestMatchers(GET, "/api/v1/orders/user").hasRole("BUYER")
                                .requestMatchers(GET, "api/v1/orders/seller").hasRole("SELLER")
                                .requestMatchers(GET, "/api/v1/orders/**").hasAnyRole("ADMIN")
                                //transactionType
                                .requestMatchers("/api/v1/transactionSystem/**").permitAll()
                                //notification
                                //.requestMatchers(PUT,"/api/v1/notifications/**").hasAnyRole("ADMIN", "STAFF")
                                .requestMatchers("/api/v1/notifications/**").permitAll()
                                //Report
                                .requestMatchers("/api/v1/report/**").permitAll()
                                .anyRequest().authenticated()


                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                // Uncomment the following for Google OAuth2 login support
                /*
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(loginGoogleSuccess)
                        .failureHandler(loginGoogleFailure)
                )
                */
                .logout(logout -> logout
                        .logoutUrl("/api/v1/auth/logout")
                        .addLogoutHandler(logoutHandler)
                        .logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.clearContext())
                );

        return http.build();
    }


}
