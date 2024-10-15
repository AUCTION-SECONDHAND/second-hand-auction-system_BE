package com.second_hand_auction_system.service.knowYourCustomer;

import com.second_hand_auction_system.dtos.request.kyc.KycDto;
import com.second_hand_auction_system.dtos.responses.ResponseObject;
import com.second_hand_auction_system.dtos.responses.kyc.KycResponse;
import com.second_hand_auction_system.models.KnowYourCustomer;
import com.second_hand_auction_system.models.User;
import com.second_hand_auction_system.repositories.KnowYourCustomerRepository;
import com.second_hand_auction_system.repositories.UserRepository;
import com.second_hand_auction_system.service.jwt.IJwtService;
import com.second_hand_auction_system.utils.KycStatus;
import com.second_hand_auction_system.utils.Role;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class KnowYourCustomerService implements IKnowYourCustomerService {
    private final KnowYourCustomerRepository knowYourCustomerRepository;
    private final IJwtService jwtService;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public ResponseEntity<?> register(KycDto kyc) {
        // Lấy Authorization header từ request
        String authHeader = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
                .getRequest().getHeader("Authorization");

        // Kiểm tra tính hợp lệ của Authorization header
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
        User requester = userRepository.findByEmailAndStatusIsTrue(email).orElse(null);

        // Kiểm tra người dùng
        if (requester == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ResponseObject.builder()
                            .status(HttpStatus.UNAUTHORIZED)
                            .message("Unauthorized request - User not found")
                            .build());
        }

        // Kiểm tra quyền người dùng
        if (requester.getRole() != Role.BUYER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResponseObject.builder()
                            .status(HttpStatus.FORBIDDEN)
                            .message("Forbidden request - Only buyers can register for KYC")
                            .build());
        }

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
                .sumbited(new Date())
                .phoneNumber(kyc.getPhoneNumber())
                .user(requester)
                .build();

        // Lưu bản ghi KYC vào cơ sở dữ liệu
        knowYourCustomerRepository.save(knowYourCustomer);

        // Tạo phản hồi KYC
        KycResponse kycResponse = KycResponse.builder()
                .kycId(knowYourCustomer.getKycId())
                .dob(kyc.getDob().toString()) // Chuyển đổi nếu cần
                .age(kyc.getAge())
                .fullName(kyc.getFullName())
                .phoneNumber(kyc.getPhoneNumber())
                .email(kyc.getEmail())
                .gender(kyc.getGender())
                .cccdNumber(kyc.getCccdNumber())
                .frontDocumentUrl(kyc.getFrontDocumentUrl())
                .backDocumentUrl(kyc.getBackDocumentUrl())
                .kycStatus(knowYourCustomer.getKycStatus())
                .submited(knowYourCustomer.getSumbited())
                .user(requester.getId()) // Gán userId
                .build();

        // Trả về phản hồi thành công
        return ResponseEntity.ok(ResponseObject.builder()
                .data(kycResponse)
                .message("KYC registration successful")
                .status(HttpStatus.OK)
                .build());
    }
}
