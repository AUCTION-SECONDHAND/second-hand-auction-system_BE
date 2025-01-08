package com.second_hand_auction_system.service.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.second_hand_auction_system.dtos.request.user.Authentication;
import com.second_hand_auction_system.dtos.request.user.ChangePassWordDTO;
import com.second_hand_auction_system.dtos.request.user.RegisterRequest;
import com.second_hand_auction_system.dtos.request.user.UserDto;
import com.second_hand_auction_system.dtos.responses.ResponseObject;
import com.second_hand_auction_system.dtos.responses.user.*;
import com.second_hand_auction_system.models.Token;
import com.second_hand_auction_system.models.User;
import com.second_hand_auction_system.models.Wallet;
import com.second_hand_auction_system.repositories.TokenRepository;
import com.second_hand_auction_system.repositories.UserRepository;
import com.second_hand_auction_system.repositories.WalletRepository;
import com.second_hand_auction_system.service.email.EmailService;
import com.second_hand_auction_system.service.email.OtpService;
import com.second_hand_auction_system.service.jwt.IJwtService;
import com.second_hand_auction_system.utils.Role;
import com.second_hand_auction_system.utils.StatusWallet;
import com.second_hand_auction_system.utils.TokenType;
import com.second_hand_auction_system.utils.WalletType;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.nio.file.attribute.UserPrincipal;
import java.security.Principal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final IJwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final TokenRepository tokenRepository;
    private final ModelMapper modelMapper;
    private final EmailService emailService;
    private final OtpService otpService;
    private final WalletRepository walletRepository;
    @Override
    public ResponseEntity<RegisterResponse> register(RegisterRequest registerRequest) {
        try {
            if (userRepository.existsByEmail(registerRequest.getEmail())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        RegisterResponse.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message("Email already in use")
                                .build()
                );
            }
            Wallet newWallet = Wallet.builder()
                    .balance(0.0)
                    .statusWallet(StatusWallet.ACTIVE)
                    .walletType(WalletType.CUSTOMER)
                    .build();
            walletRepository.save(newWallet);  // Lưu ví vào cơ sở dữ liệu

            User newUser = User.builder()
                    .email(registerRequest.getEmail())
                    .password(passwordEncoder.encode(registerRequest.getPassword()))
                    .role(Role.BUYER)
                    .avatar("https://nld.mediacdn.vn/thumb_w/698/2017/stvalentine1-1489973474049.jpg")
                    .fullName(registerRequest.getFullName())
                    .phoneNumber(registerRequest.getPhoneNumber())
                    .status(false)
                    .wallet(newWallet)
                    .build();

            userRepository.save(newUser);
            if (newUser.getId() != null) {
                newWallet.setUser(newUser);
                walletRepository.save(newWallet);

                emailService.sendOtp(newUser.getEmail(), newUser.getId());

            }

            return ResponseEntity.ok(RegisterResponse.builder()
                    .status(HttpStatus.OK.value())
                    .message("User registered successfully")
                    .build());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    RegisterResponse.builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Registration failed")
                            .build()
            );
        }
    }

    @Override
    public ResponseEntity<AuthenticationResponse> login(Authentication authenticationRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authenticationRequest.getEmail(),
                            authenticationRequest.getPassword()
                    )
            );

            User user = userRepository.findByEmailAndStatusIsTrue(authenticationRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String jwtToken = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            revokeAllUserTokens(user);
            saveToken(user, jwtToken, refreshToken);
            UserResponse userResponse = modelMapper.map(user, UserResponse.class);
            AuthenticationResponse.ResponseData responseData = AuthenticationResponse.ResponseData.builder()
                    .user(userResponse)
                    .token(jwtToken)
                    .refreshToken(refreshToken)
                    .build();

            return ResponseEntity.ok(AuthenticationResponse.builder()
                    .status(HttpStatus.OK.value())
                    .message("Login successful")
                    .data(responseData)
                    .build());

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    AuthenticationResponse.builder()
                            .status(HttpStatus.NOT_FOUND.value())
                            .message("Your email or password is incorrect")
                            .build()
            );
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    AuthenticationResponse.builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("An error occurred")
                            .build()
            );
        }
    }

    @Override
    public void refresh(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        refreshToken = authHeader.substring(7);
        userEmail = jwtService.extractUserEmail(refreshToken);
        if (userEmail != null) {
            var user = this.userRepository.findByEmailAndStatusIsTrue(userEmail).orElseThrow();
            if (jwtService.isTokenValid(refreshToken, user)) {
                var newToken = jwtService.generateToken(user);
                revokeAllUserTokens(user);
                saveToken(user, newToken, refreshToken);
                var authResponse = AuthenticationResponse.builder()
                        .status(200)
                        .message("Successfully")
                        .data(AuthenticationResponse.ResponseData.builder()
                                .token(newToken)
                                .refreshToken(refreshToken)
                                .user(null)
                                .build())
                        .build();
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
    }
    @Override
    public ResponseEntity<?> getListUser(int page, int limit) {
        try {
            Pageable pageable = PageRequest.of(page, limit, Sort.by(Sort.Order.desc("createAt")));

            // Lấy token từ header Authorization
            String authHeader = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
                    .getRequest().getHeader("Authorization");

            // Kiểm tra nếu Authorization header không tồn tại hoặc không hợp lệ
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ListUserResponse.builder()
                                .users(null)
                                .message("Missing or invalid Authorization header")
                                .build());
            }

            String token = authHeader.substring(7);
            String userEmail = jwtService.extractUserEmail(token);
            var requester = userRepository.findByEmailAndStatusIsTrue(userEmail).orElse(null);

            if (requester == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ListUserResponse.builder()
                                .users(null)
                                .message("Unauthorized request - User not found")
                                .build());
            }

            if (!Role.ADMIN.equals(requester.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ListUserResponse.builder()
                                .users(null)
                                .message("Access denied - Admin role required")
                                .build());
            }

            // Phân trang và lấy dữ liệu người dùng
            Page<User> userPage = userRepository.findAll(pageable);

            // Chuyển đổi sang danh sách UserResponse
            List<UserResponse> userResponses = userPage.stream()
                    .map(user -> modelMapper.map(user, UserResponse.class))
                    .collect(Collectors.toList());

            // Tạo phản hồi
            Map<String, Object> response = new HashMap<>();
            response.put("list", userResponses);
            response.put("totalPages", userPage.getTotalPages()); // Lấy tổng số trang
            response.put("totalElements", userPage.getTotalElements()); // Lấy tổng số phần tử

            return ResponseEntity.status(HttpStatus.OK)
                    .body(ResponseObject.builder()
                            .status(HttpStatus.OK)
                            .data(response)
                            .message("Success")
                            .build());

        } catch (Exception e) {
            e.printStackTrace();
            // Trả về lỗi 500 nếu có exception xảy ra
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ListUserResponse.builder()
                            .users(null)
                            .message("An error occurred: " + e.getMessage())
                            .build());
        }
    }



    @Override
    public ResponseEntity<?> isValidOtp(String email, String otp) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    RegisterResponse.builder()
                            .status(HttpStatus.NOT_FOUND.value())
                            .message("User not found")
                            .build()
            );
        }
        boolean storedOtp = otpService.isValidOtp(email, otp);
        user.setStatus(true);
        userRepository.save(user);
        if (!storedOtp) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    RegisterResponse.builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message("Invalid OTP")
                            .build()
            );
        }
        return ResponseEntity.status(HttpStatus.OK).body(
                RegisterResponse.builder()
                        .status(HttpStatus.OK.value())
                        .message("Verify success ")
                        .build()
        );
    }

    @Override
    public ResponseEntity<?> registerStaff(RegisterRequest registerRequest) {
        try {
            if (userRepository.existsByEmail(registerRequest.getEmail())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        RegisterResponse.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message("Email already in use")
                                .build()
                );
            }

            User newUser = User.builder()
                    .email(registerRequest.getEmail())
                    .password(passwordEncoder.encode(registerRequest.getPassword()))
                    .role(Role.STAFF)
                    .fullName(registerRequest.getFullName())
                    .phoneNumber(registerRequest.getPhoneNumber())
                    .avatar("https://png.pngtree.com/png-clipart/20231216/original/pngtree-vector-office-worker-staff-avatar-employee-icon-png-image_13863941.png")
                    .status(true)
                    .build();
            userRepository.save(newUser);
//            emailService.sendAccountStaff(newUser.getEmail(), newUser.getPassword());
            return ResponseEntity.ok(RegisterResponse.builder()
                    .status(HttpStatus.OK.value())
                    .message("Staff registered successfully")
                    .build());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    RegisterResponse.builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Registration failed")
                            .build()
            );
        }
    }

    @Override
    public ResponseEntity<?> updateUser( UserDto userRequest) {
        try {
            String authHeader = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
                    .getRequest().getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ListUserResponse.builder()
                                .users(null)
                                .message("Missing or invalid Authorization header")
                                .build());
            }
            String token = authHeader.substring(7);
            String userEmail = jwtService.extractUserEmail(token);
            var user = userRepository.findByEmailAndStatusIsTrue(userEmail).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(RegisterResponse.builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .message("User not found")
                        .data(null)
                        .build());
            }
            user.setFullName(userRequest.getFullName());
            user.setPhoneNumber(userRequest.getPhoneNumber());
//            user.setEmail(userRequest.getEmail());
            user.setAvatar(userRequest.getAvatarUrl());
            userRepository.save(user);
            UserResponse userResponse = modelMapper.map(user, UserResponse.class);
            return ResponseEntity.ok(RegisterResponse.builder()
                    .status(HttpStatus.OK.value())
                    .data(userResponse)
                    .message("User updated successfully")
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RegisterResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .data(null)
                .message("Update failed")
                .build());
    }

    @Override
    public ResponseEntity<?> getUserId() {
        String authHeader = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
                .getRequest().getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ListUserResponse.builder()
                            .users(null)
                            .message("Missing or invalid Authorization header")
                            .build());
        }
        String token = authHeader.substring(7);
        String userEmail = jwtService.extractUserEmail(token);
        var requester = userRepository.findByEmailAndStatusIsTrue(userEmail).orElse(null);
        if (requester == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ListUserResponse.builder()
                            .users(null)
                            .message("Unauthorized request - User not found")
                            .build());
        }
        User user = userRepository.findById(requester.getId()).orElse(null);
        if (user != null) {
            UserResponse userResponse = modelMapper.map(user, UserResponse.class);

            userResponse.setBalance(user.getWallet().getBalance());

            return ResponseEntity.ok(ResponseObject.builder()
                    .status(HttpStatus.OK)
                    .message("User found")
                    .data(userResponse)
                    .build());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                .status(HttpStatus.NOT_FOUND)
                .data(null)
                .message("User not found")
                .build());
    }

    @Override
    public ResponseEntity<?> deleteUser(int id) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setStatus(false);
            userRepository.save(user);
            return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder()
                    .data(null)
                    .status(HttpStatus.OK)
                    .message("User deleted")
                    .build());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                .data(null)
                .status(HttpStatus.NOT_FOUND)
                .message("User not found")
                .build());

    }

    @Override
    public ResponseEntity<?> forgotPassword(String email) throws MessagingException {
        var user = userRepository.findByEmail(email).orElse(null);
        if(user != null){
            emailService.sendSetPassword(user.getEmail());
            return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder().status(HttpStatus.OK)
                    .message("Forgot password success")
                    .data(null)
                    .build());
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder().status(HttpStatus.NOT_FOUND)
                .message("Forgot password failed")
                .data(null)
                .build());
    }

    @Override
    public ResponseEntity<?> changePassword(ChangePassWordDTO request, Principal connectedUser) {
        User user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResponseObject.builder()
                    .status(HttpStatus.UNAUTHORIZED)
                    .message("Password does not match")
                    .data(null)
                    .build());
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseObject.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Password does not match confirm password")
                    .build());
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder()
                .status(HttpStatus.OK)
                .message("Change password successful")
                .build());
    }

    @Override
    public Map<String, Object> getUserComparison() {
        LocalDate currentDate = LocalDate.now();
        int currentMonth = currentDate.getMonthValue();
        int currentYear = currentDate.getYear();

        // Lấy số lượng người dùng trong tháng này
        Long currentMonthUsers = userRepository.countUsersByMonthAndYear(currentMonth, currentYear);

        // Lấy số lượng người dùng trong tháng trước
        int previousMonth = (currentMonth == 1) ? 12 : currentMonth - 1;
        int previousYear = (currentMonth == 1) ? currentYear - 1 : currentYear;

        Long previousMonthUsers = userRepository.countUsersByPreviousMonth(previousMonth, previousYear);

        // Tính sự thay đổi
        int change = (previousMonthUsers != 0) ? (int) (currentMonthUsers - previousMonthUsers) : 0;

        Map<String, Object> response = new HashMap<>();
        response.put("currentMonthUsers", currentMonthUsers);
        response.put("previousMonthUsers", previousMonthUsers);
        response.put("change", change);

        return response;
    }

    @Override
    public ResponseEntity<?> countSellerByWeek() {
        String authHeader = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest().getHeader("Authorization");
        if(authHeader == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResponseObject.builder().status(HttpStatus.UNAUTHORIZED).data(null).message("Unauthorized")
                    .build());
        }
        String token = authHeader.substring(7);
        String userEmail = jwtService.extractUserEmail(token);
        var requester = userRepository.findByEmailAndStatusIsTrue(userEmail).orElse(null);
        if (requester == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                    .status(HttpStatus.NOT_FOUND)
                    .data(null)
                    .message("User not found")
                    .build());
        }
        LocalDate currentDay = LocalDate.now();
        LocalDate startOfWeek = currentDay.with(DayOfWeek.MONDAY);
        LocalDate sundayOfWeek = currentDay.with(DayOfWeek.SUNDAY);
        Map<String,Long> sellerByWeek = new LinkedHashMap<>();
        for (LocalDate date = startOfWeek; !date.isAfter(sundayOfWeek); date = date.plusDays(1)) {
            sellerByWeek.put(date.getDayOfWeek().toString(),
                    userRepository.countByCreateAtBetween(date.atStartOfDay(),date.plusDays(1).atStartOfDay()));
        }
        return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder()
                .status(HttpStatus.OK)
                .data(sellerByWeek)
                .message("Data seller")
                .build());
    }


    private void saveToken(User user, String jwtToken, String refreshToken) {
        Token token = Token.builder()
                .user(user)
                .token(jwtToken)
                .refreshToken(refreshToken)
                .tokenType(TokenType.BEARER)
                .revoked(false)
                .expired(false)
                .build();
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(User user) {
        var tokenList = tokenRepository.findAllUserTokenByUserId(user.getId());
        tokenList.forEach(token -> {
            token.setRevoked(true);
            token.setExpired(true);
        });
        tokenRepository.saveAll(tokenList);
    }

}
