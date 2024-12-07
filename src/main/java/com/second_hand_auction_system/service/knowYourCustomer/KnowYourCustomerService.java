package com.second_hand_auction_system.service.knowYourCustomer;

import com.second_hand_auction_system.dtos.request.kyc.ApproveKyc;
import com.second_hand_auction_system.dtos.request.kyc.KycDto;
import com.second_hand_auction_system.dtos.responses.ResponseObject;
import com.second_hand_auction_system.dtos.responses.address.AddressResponse;
import com.second_hand_auction_system.dtos.responses.kyc.KycResponse;
import com.second_hand_auction_system.models.Address;
import com.second_hand_auction_system.models.KnowYourCustomer;
import com.second_hand_auction_system.models.SellerInformation;
import com.second_hand_auction_system.models.User;
import com.second_hand_auction_system.repositories.*;
import com.second_hand_auction_system.service.email.EmailService;
import com.second_hand_auction_system.service.jwt.IJwtService;
import com.second_hand_auction_system.service.notification.NotificationService;
import com.second_hand_auction_system.utils.KycStatus;
import com.second_hand_auction_system.utils.Role;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class KnowYourCustomerService implements IKnowYourCustomerService {
    private final KnowYourCustomerRepository knowYourCustomerRepository;
    private final IJwtService jwtService;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final EmailService emailService;
    private final AddressRepository addressRepository;
    private final NotificationService notificationService;
    private final WithdrawRequestRepository withdrawRequestRepository;

    private final SimpMessagingTemplate messagingTemplate;
    private final SellerInformationRepository sellerInformationRepository;


    @Override
    @Transactional
    public ResponseEntity<?> register(KycDto kycDto) {
        // Lấy header Authorization từ yêu cầu
        String authHeader = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
                .getRequest().getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ResponseObject.builder()
                            .data(null)
                            .message("Missing or invalid Authorization header")
                            .status(HttpStatus.UNAUTHORIZED)
                            .build());
        }

        // Trích xuất token từ header và lấy email người dùng
        String token = authHeader.substring(7);
        String email = jwtService.extractUserEmail(token);

        // Kiểm tra người dùng dựa trên email
        User requester = userRepository.findByEmailAndStatusIsTrue(email).orElse(null);
        if (requester == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ResponseObject.builder()
                            .status(HttpStatus.UNAUTHORIZED)
                            .message("Unauthorized request - User not found")
                            .build());
        }

        // Kiểm tra vai trò của người dùng, chỉ cho phép người dùng có vai trò BUYER
        if (requester.getRole() != Role.BUYER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResponseObject.builder()
                            .status(HttpStatus.FORBIDDEN)
                            .message("Forbidden request - Only buyers can register for KYC")
                            .build());
        }

        // Kiểm tra xem CCCD đã được sử dụng chưa
        Optional<KnowYourCustomer> existingByCccd = knowYourCustomerRepository.findByCccdNumber(kycDto.getCccdNumber());
        if (existingByCccd.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ResponseObject.builder()
                            .status(HttpStatus.CONFLICT)
                            .message("The provided CCCD number has already been used for KYC registration")
                            .build());
        }

        // Kiểm tra xem KYC đã tồn tại và có trạng thái PENDING hay không
        Optional<KnowYourCustomer> existingKyc = knowYourCustomerRepository.findByUserIdAndKycStatus(requester.getId(), KycStatus.PENDING);
        if (existingKyc.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ResponseObject.builder()
                            .status(HttpStatus.CONFLICT)
                            .message("KYC registration already exists and is pending for this user")
                            .build());
        }

        // Tạo đối tượng KnowYourCustomer để lưu vào cơ sở dữ liệu
        KnowYourCustomer knowYourCustomer = KnowYourCustomer.builder()
                .kycStatus(KycStatus.PENDING)
                .submitted(new Date())
                .user(requester)
                .fullName(kycDto.getFullName())
                .verifiedBy("")
                .nationality(kycDto.getNationality())
                .permanentAddress(kycDto.getPermanentAddress())
                .gender(kycDto.getGender())
                .dateOfBirth(kycDto.getDob())
                .reason("")
                .home(kycDto.getHome())
                .image(kycDto.getImage())
                .cccdNumber(kycDto.getCccdNumber())
                .build();

        // Lưu đối tượng KnowYourCustomer vào cơ sở dữ liệu
        knowYourCustomerRepository.save(knowYourCustomer);

        // Tạo phản hồi cho KYC đã đăng ký
        KycResponse kycResponse = KycResponse.builder()
                .kycId(knowYourCustomer.getKycId())
                .kycStatus(knowYourCustomer.getKycStatus())
                .submitted(knowYourCustomer.getSubmitted())
                .nationality(knowYourCustomer.getNationality())
                .permanentAddress(knowYourCustomer.getPermanentAddress())
                .gender(knowYourCustomer.getGender())
                .dob(knowYourCustomer.getDateOfBirth())
                .cccdNumber(knowYourCustomer.getCccdNumber())
                .userId(requester.getId())
                .fullName(knowYourCustomer.getFullName())
                .verified_by("")
                .reason(knowYourCustomer.getReason())
                .build();

        return ResponseEntity.ok(ResponseObject.builder()
                .data(kycResponse)
                .message("KYC registration successful")
                .status(HttpStatus.OK)
                .build());
    }




    @Override
    public ResponseEntity<?> approveKyc(ApproveKyc kycDto, int kycId) throws MessagingException {
        String authHeader = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
                .getRequest().getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ResponseObject.builder()
                            .data(null)
                            .message("Missing or invalid Authorization header")
                            .status(HttpStatus.UNAUTHORIZED)
                            .build());
        }

        String token = authHeader.substring(7);
        String email = jwtService.extractUserEmail(token);
        User requester = userRepository.findByEmailAndStatusIsTrue(email).orElse(null);
        if (requester == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ResponseObject.builder()
                            .status(HttpStatus.UNAUTHORIZED)
                            .message("Unauthorized request - User not found")
                            .build());
        }

        Optional<KnowYourCustomer> optionalKyc = knowYourCustomerRepository.findById(kycId);
        if (optionalKyc.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                    .status(HttpStatus.NOT_FOUND)
                    .message("KYC not found with id: " + kycId)
                    .data(null)
                    .build());
        }

        KnowYourCustomer kyc = optionalKyc.get();
        kyc.setKycStatus(kycDto.getStatus());
        kyc.setVerifiedBy(requester.getFullName());
        kyc.setReason(kycDto.getReason());
        KnowYourCustomer knowYourCustomer = knowYourCustomerRepository.save(kyc);
        User user = knowYourCustomerRepository.findUserByKycId(kyc.getKycId()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                    .status(HttpStatus.NOT_FOUND)
                    .data(null)
                    .message("User not found")
                    .build());
        }
//        Address address = addressRepository.findByUserIdAndStatusIsTrue(user.getId()).orElse(null);
//        assert  address != null;
        if (knowYourCustomer.getKycStatus().equals(KycStatus.APPROVED)) {
            user.setRole(Role.SELLER);
            SellerInformation sellerInformation = SellerInformation.builder()
                    .sellerId(requester.getId())
                    .avatar(requester.getAvatar())
                    .address("")
                    .storeName(requester.getFullName())
                    .user(requester)
                    .build();
            sellerInformationRepository.save(sellerInformation);
        } else if (knowYourCustomer.getKycStatus().equals(KycStatus.PENDING) ||
                knowYourCustomer.getKycStatus().equals(KycStatus.REJECTED)) {
            user.setRole(Role.BUYER);  // Update the user's role to BUYER
        }
        userRepository.save(user);


        emailService.sendKycSuccessNotification(user.getEmail(), user.getFullName()); // Send email notification for KYC success
        return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder()
                .status(HttpStatus.OK)
                .data(null)
                .message("Approve KYC successful. User has been upgraded to SELLER.")
                .build());
    }


    @Override
    public ResponseEntity<?> getKycById(int kycId) {
        var kyc = knowYourCustomerRepository.findById(kycId).orElse(null);
        if (kyc != null) {
            KycResponse kycResponse = KycResponse.builder()
                    .kycId(kycId)
                    .userId(kyc.getKycId())
                    .dob(kyc.getDateOfBirth())
                    .gender(kyc.getGender())
                    .cccdNumber(kyc.getCccdNumber())
                    .kycStatus(kyc.getKycStatus())
                    .submitted(kyc.getSubmitted())
                    .permanentAddress(kyc.getPermanentAddress())
                    .nationality(kyc.getNationality())
                    .fullName(kyc.getFullName())
                    .home(kyc.getHome())
                    .imageUrl(kyc.getImage())
                    .build();

            return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder()
                    .status(HttpStatus.OK)
                    .data(kycResponse)
                    .message("KYC record with id: " + kycId)
                    .build());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                .status(HttpStatus.NOT_FOUND)
                .data(null)
                .message("KYC not found with id: " + kycId)
                .build());
    }


    @Override
    public ResponseEntity<?> getKycs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createAt")));
        Page<KnowYourCustomer> kycPage = knowYourCustomerRepository.findAll(pageable);

        List<KycResponse> kycResponses = kycPage.getContent()
                .stream()
                .map(kyc -> KycResponse.builder()
                        .kycId(kyc.getKycId())
                        .dob(kyc.getDateOfBirth() != null ? kyc.getDateOfBirth() : null)
                        .fullName(kyc.getFullName())
                        .gender(kyc.getGender())
                        .cccdNumber(kyc.getCccdNumber())
                        .kycStatus(kyc.getKycStatus())
                        .permanentAddress(kyc.getPermanentAddress())
                        .nationality(kyc.getNationality())
                        .submitted(kyc.getSubmitted())
                        .userId(kyc.getUser() != null ? kyc.getUser().getId() : null)
                        .build())
                .collect(Collectors.toList());
        Map<String, Object> response = new HashMap<>();
        response.put("data", kycResponses);
        response.put("totalPages", kycPage.getTotalPages());
        response.put("totalElements", kycPage.getTotalElements());
        response.put("status", HttpStatus.OK);
        response.put("message", "Fetched Kycs");
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<?> updateKyc(KycDto kycDto) {
        String authHeader = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
                .getRequest().getHeader("Authorization");

        // Kiểm tra header Authorization
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ResponseObject.builder()
                            .data(null)
                            .message("Missing or invalid Authorization header")
                            .status(HttpStatus.UNAUTHORIZED)
                            .build());
        }

        // Trích xuất email từ token
        String token = authHeader.substring(7);
        String email;
        try {
            email = jwtService.extractUserEmail(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ResponseObject.builder()
                            .status(HttpStatus.UNAUTHORIZED)
                            .message("Token is invalid or expired")
                            .data(null)
                            .build()
            );
        }

        // Kiểm tra người dùng
        User requester = userRepository.findByEmailAndStatusIsTrue(email).orElse(null);
        if (requester == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ResponseObject.builder()
                            .status(HttpStatus.UNAUTHORIZED)
                            .message("Unauthorized request - User not found")
                            .data(null)
                            .build());
        }

        // Tìm KYC của người dùng
        var kyc = knowYourCustomerRepository.findByUserId(requester.getId()).orElse(null);
        if (kyc == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .status(HttpStatus.NOT_FOUND)
                            .message("KYC not found")
                            .data(null)
                            .build()
            );
        }

        // Cập nhật thông tin KYC
//        kyc.setFullName(kycDto.getFullName());
//        kyc.setGender(kycDto.getGender());
//        kyc.setCccdNumber(kycDto.getCccdNumber());
//        kyc.setFrontDocumentUrl(kycDto.getFrontDocumentUrl());
//        kyc.setBackDocumentUrl(kycDto.getBackDocumentUrl());
//        kyc.setDateOfBirth(kycDto.getDob());
//        kyc.setEmail(kycDto.getEmail());
//        kyc.setPhoneNumber(kycDto.getPhoneNumber());

        // Lưu thông tin KYC đã cập nhật
        knowYourCustomerRepository.save(kyc);
        KycResponse kycResponse = KycResponse.builder()
                .kycId(kyc.getKycId())
                .kycStatus(kyc.getKycStatus())
                .permanentAddress(kyc.getPermanentAddress())
                .submitted(kyc.getSubmitted())
                .userId(kyc.getUser().getId())
                .dob(kyc.getDateOfBirth() != null ? kyc.getDateOfBirth().toString() : null)
                .fullName(kyc.getFullName())
                .gender(kyc.getGender())
                .cccdNumber(kyc.getCccdNumber())
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseObject.builder()
                        .status(HttpStatus.OK)
                        .message("Update success")
                        .data(kycResponse) // Trả lại KYC đã được cập nhật
                        .build()
        );
    }




    @Override
    public ResponseEntity<?> getKycUserById() {
        String authHeader = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
                .getRequest().getHeader("Authorization");

        // Kiểm tra header Authorization
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ResponseObject.builder()
                            .data(null)
                            .message("Missing or invalid Authorization header")
                            .status(HttpStatus.UNAUTHORIZED)
                            .build());
        }

        // Trích xuất email từ token
        String token = authHeader.substring(7);
        String email;
        try {
            email = jwtService.extractUserEmail(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ResponseObject.builder()
                            .status(HttpStatus.UNAUTHORIZED)
                            .message("Token is invalid or expired")
                            .data(null)
                            .build()
            );
        }

        // Kiểm tra người dùng
        User requester = userRepository.findByEmailAndStatusIsTrue(email).orElse(null);
        if (requester == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ResponseObject.builder()
                            .status(HttpStatus.UNAUTHORIZED)
                            .message("Unauthorized request - User not found")
                            .data(null)
                            .build());
        }
        var kyc = knowYourCustomerRepository.findByUserId(requester.getId()).orElse(null);
        if (kyc == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                    .message("Kyc not found")
                    .data(null)
                    .status(HttpStatus.NOT_FOUND)
                    .build());
        }
        KycResponse kycResponse =KycResponse.builder()
                .kycId(kyc.getKycId())
                .dob(kyc.getDateOfBirth() != null ? kyc.getDateOfBirth() : null)
                .fullName(kyc.getFullName())
                .gender(kyc.getGender())
                .cccdNumber(kyc.getCccdNumber())
                .kycStatus(kyc.getKycStatus())
                .permanentAddress(kyc.getPermanentAddress())
                .nationality(kyc.getNationality())
                .submitted(kyc.getSubmitted())
                .userId(requester.getId())
                .home(kyc.getHome())
                .reason(kyc.getReason())
                .build();
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseObject.builder()
                        .data(kycResponse)
                        .message("User found")
                        .status(HttpStatus.OK)
                        .build());
    }




}
