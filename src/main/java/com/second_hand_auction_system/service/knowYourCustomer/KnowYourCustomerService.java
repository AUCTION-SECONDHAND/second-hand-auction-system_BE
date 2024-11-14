package com.second_hand_auction_system.service.knowYourCustomer;

import com.second_hand_auction_system.dtos.request.kyc.ApproveKyc;
import com.second_hand_auction_system.dtos.request.kyc.KycDto;
import com.second_hand_auction_system.dtos.responses.ResponseObject;
import com.second_hand_auction_system.dtos.responses.address.AddressResponse;
import com.second_hand_auction_system.dtos.responses.kyc.KycResponse;
import com.second_hand_auction_system.models.KnowYourCustomer;
import com.second_hand_auction_system.models.User;
import com.second_hand_auction_system.repositories.AddressRepository;
import com.second_hand_auction_system.repositories.KnowYourCustomerRepository;
import com.second_hand_auction_system.repositories.UserRepository;
import com.second_hand_auction_system.repositories.WithdrawRequestRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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

    @Override
    @Transactional
    public ResponseEntity<?> register(KycDto kyc) {
        // Lấy header Authorization từ yêu cầu
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
        String email = jwtService.extractUserEmail(token);

        // Kiểm tra người dùng
        User requester = userRepository.findByEmailAndStatusIsTrue(email).orElse(null);
        if (requester == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ResponseObject.builder()
                            .status(HttpStatus.UNAUTHORIZED)
                            .message("Unauthorized request - User not found")
                            .build());
        }

        // Kiểm tra vai trò của người dùng
        if (requester.getRole() != Role.BUYER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResponseObject.builder()
                            .status(HttpStatus.FORBIDDEN)
                            .message("Forbidden request - Only buyers can register for KYC")
                            .build());
        }

        // Kiểm tra xem KYC đã tồn tại hay chưa và trạng thái là PENDING
        Optional<KnowYourCustomer> existingKyc = knowYourCustomerRepository.findByUserIdAndKycStatus(requester.getId(), KycStatus.PENDING);
        if (existingKyc.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ResponseObject.builder()
                            .status(HttpStatus.CONFLICT)
                            .message("KYC registration already exists and is pending for this user")
                            .build());
        }

        // Kiểm tra xem KYC đã tồn tại hay chưa
        if (knowYourCustomerRepository.existsByCccdNumber(kyc.getCccdNumber())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ResponseObject.builder()
                            .status(HttpStatus.CONFLICT)
                            .message("KYC record already exists for this CCCD number")
                            .build());
        }

        KnowYourCustomer knowYourCustomer = KnowYourCustomer.builder()
                .age(kyc.getAge())
                .dateOfBirth(kyc.getDob())
                .email(kyc.getEmail())
                .cccdNumber(kyc.getCccdNumber())
                .backDocumentUrl(kyc.getBackDocumentUrl())
                .frontDocumentUrl(kyc.getFrontDocumentUrl())
                .fullName(kyc.getFullName())
                .kycStatus(KycStatus.PENDING)
                .gender(kyc.getGender())
                .submitted(new Date())
                .phoneNumber(kyc.getPhoneNumber())
                .user(requester)
                .verifiedBy(requester.getFullName())
                .build();

        // Lưu đối tượng KYC vào cơ sở dữ liệu
        knowYourCustomerRepository.save(knowYourCustomer);

        // Tạo phản hồi KYC
        KycResponse kycResponse = KycResponse.builder()
                .kycId(knowYourCustomer.getKycId())
                .dob(kyc.getDob().toString())
                .age(kyc.getAge())
                .fullName(kyc.getFullName())
                .phoneNumber(kyc.getPhoneNumber())
                .email(kyc.getEmail())
                .gender(kyc.getGender())
                .cccdNumber(kyc.getCccdNumber())
                .frontDocumentUrl(knowYourCustomer.getFrontDocumentUrl())
                .backDocumentUrl(knowYourCustomer.getBackDocumentUrl())
                .kycStatus(knowYourCustomer.getKycStatus())
                .submited(knowYourCustomer.getSubmitted())
                .userId(requester.getId()) // Gán userId
                .verified_by(knowYourCustomer.getVerifiedBy())
                .reason(null)
                .build();


        // Gửi thông báo qua WebSocket
//        try {
//            messagingTemplate.convertAndSend("/topic/kyc", kycResponse);
//        } catch (Exception e) {
//            log.error("Error sending WebSocket message: ", e);
//        }

        // Trả về phản hồi thành công
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

        if (knowYourCustomer.getKycStatus().equals(KycStatus.APPROVED)) {
            user.setRole(Role.SELLER);  // Update the user's role to SELLER
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
            var address = addressRepository.findByUserIdAndStatusIsTrue(kyc.getUser().getId()).orElse(null);
            AddressResponse addressResponse = address != null ?
                    modelMapper.map(address, AddressResponse.class) :
                    new AddressResponse(); // Create an empty or default AddressResponse

            // Set default values in AddressResponse if address is null
            if (address == null) {
                addressResponse.setProvince_name("N/A");
                addressResponse.setDistrict_name("N/A");
                addressResponse.setWard_name("N/A");
                addressResponse.setAddress_name("N/A");
            }

            KycResponse kycResponse = KycResponse.builder()
                    .kycId(kycId)
                    .userId(kyc.getKycId())
                    .dob(kyc.getDateOfBirth().toString())
                    .email(kyc.getEmail())
                    .gender(kyc.getGender())
                    .cccdNumber(kyc.getCccdNumber())
                    .frontDocumentUrl(kyc.getFrontDocumentUrl())
                    .backDocumentUrl(kyc.getBackDocumentUrl())
                    .kycStatus(kyc.getKycStatus())
                    .submited(kyc.getSubmitted())
                    .phoneNumber(kyc.getPhoneNumber())
                    .fullName(kyc.getFullName())
                    .address(addressResponse)
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
                        .dob(kyc.getDateOfBirth() != null ? kyc.getDateOfBirth().toString() : null)
                        .age(kyc.getAge())
                        .fullName(kyc.getFullName())
                        .phoneNumber(kyc.getPhoneNumber())
                        .email(kyc.getUser() != null ? kyc.getUser().getEmail() : null)
                        .gender(kyc.getGender())
                        .cccdNumber(kyc.getCccdNumber())
                        .frontDocumentUrl(kyc.getFrontDocumentUrl())
                        .backDocumentUrl(kyc.getBackDocumentUrl())
                        .kycStatus(kyc.getKycStatus())
                        .submited(kyc.getSubmitted())
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

        var kyc = knowYourCustomerRepository.findByUserId(requester.getId()).orElse(null);
        if (kyc == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .status(HttpStatus.NOT_FOUND)
                            .message("Kyc not found")
                            .data(null)
                            .build()
            );
        }

        // Cập nhật thông tin KYC
        kyc.setAge(kycDto.getAge());
        kyc.setFullName(kycDto.getFullName());
        kyc.setGender(kycDto.getGender());
        kyc.setCccdNumber(kycDto.getCccdNumber());
        kyc.setFrontDocumentUrl(kycDto.getFrontDocumentUrl());
        kyc.setBackDocumentUrl(kycDto.getBackDocumentUrl());
        kyc.setDateOfBirth(kycDto.getDob());
        kyc.setEmail(kycDto.getEmail());
        kyc.setPhoneNumber(kycDto.getPhoneNumber());
        knowYourCustomerRepository.save(kyc);

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseObject.builder()
                        .status(HttpStatus.OK)
                        .message("Update success")
                        .data(kyc) // Trả lại kyc đã được cập nhật
                        .build()
        );
    }



}
