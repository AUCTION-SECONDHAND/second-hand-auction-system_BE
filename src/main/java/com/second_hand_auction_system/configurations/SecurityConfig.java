package com.second_hand_auction_system.configurations;

import com.second_hand_auction_system.utils.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;

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
            "api/v1/auctions/**",
            "/api/v1/user/**",
            "api/v1/user/forgot-password/**",
            "api/v1/withdrawRequest/vnpay-payment/**",
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(WHITE_LIST).permitAll()
                        //user
                        .requestMatchers(GET, "/api/v1/user/**").hasRole("ADMIN")
                        .requestMatchers(POST, "/api/v1/user/**").hasRole("ADMIN")
                        .requestMatchers(PUT,"/api/v1/user/**").hasAnyRole("SELLER", "BUYER")
                        .requestMatchers(PATCH,"/api/v1/user/**").permitAll()
                        .requestMatchers(DELETE,"/api/v1/user/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/feedback/**").permitAll()
                        .requestMatchers("/api/v1/seller-information/**").permitAll()

                        //main-category
                        .requestMatchers(POST, "/api/v1/main-category/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/api/v1/main-category/**").permitAll()
                        //sub-category
                        .requestMatchers(POST, "/api/v1/sub-category/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/api/v1/item/**").permitAll()
                        //wallet-customer
                        .requestMatchers(POST, "/api/v1/walletCustomer/**").hasAnyRole("BUYER", "SELLER")
                        .requestMatchers(GET,"/api/v1/walletCustomer/**").permitAll()
                        //item
                        .requestMatchers(POST, "/api/v1/item/**").hasRole("SELLER")
                        .requestMatchers(PUT, "/api/v1/item/**").hasRole("STAFF")
                        //transaction-wallet
                        .requestMatchers("/api/v1/transactionWallet/**").hasAnyRole("BUYER", "SELLER")

                        //kyc
                        .requestMatchers(POST,"/api/v1/kyc/**").hasRole("BUYER")
                        .requestMatchers(PUT,"/api/v1/kyc").hasRole("SELLER")
                        .requestMatchers(PUT,"/api/v1/kyc/approve/**").hasRole("STAFF")
                        .requestMatchers(GET,"/api/v1/kyc/**").permitAll()



                        //auction
                        .requestMatchers(POST,"/api/v1/auction-register/**").hasRole("BUYER")
                        .requestMatchers(POST,"api/v1/auction/**").hasRole("STAFF")
                        .requestMatchers("/api/v1/address/**").permitAll()
                        .requestMatchers("/api/v1/bids/**").permitAll()
                        //withdraw
                        .requestMatchers(POST,"/api/v1/withdrawRequest/**").hasRole("SELLER")
                        .requestMatchers(PUT,"/api/v1/withdrawRequest/**").hasRole("STAFF")
                        .requestMatchers(GET,"api/v1/withdrawRequest/**").permitAll()
                        //auction_type
                        .requestMatchers("/api/v1/auctionType/**").hasAnyRole("ADMIN", "STAFF")
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
