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

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

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
            "/api/v1/auth/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(WHITE_LIST).permitAll()
                        .requestMatchers(GET, "/api/v1/user/**").permitAll()
                        .requestMatchers(POST, "/api/v1/user/**").hasRole("ADMIN")
                        .requestMatchers(POST, "/api/v1/walletCustomer/**").permitAll()
                        .requestMatchers("/api/v1/main-category/**").permitAll()
                        .requestMatchers(POST, "/api/v1/main-category/**").hasAnyAuthority(Role.ADMIN.name(),Role.STAFF.name())
                        .requestMatchers(POST,"/api/v1/sub-category/**").hasAnyAuthority(Role.ADMIN.name(),Role.STAFF.name())
                        .requestMatchers("/api/v1/item/**").permitAll()
                        .requestMatchers("/api/v1/auction/**").permitAll()
                        .requestMatchers(POST, "/api/v1/item/**").hasAuthority(Role.SELLER.name())
                        .requestMatchers("api/v1/transactionWallet/**").hasAuthority(Role.ADMIN.name())
                        .requestMatchers("/api/v1/kyc/**").hasAuthority(Role.STAFF.name())
                        .requestMatchers("/api/v1/auction-register/**").permitAll()
                        .requestMatchers("/api/v1/address/**").permitAll()
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
