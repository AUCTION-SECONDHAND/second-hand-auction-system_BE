package com.second_hand_auction_system.service.walletCustomer;

import com.second_hand_auction_system.dtos.request.walletCustomer.Deposit;
import com.second_hand_auction_system.dtos.responses.ResponseObject;
import com.second_hand_auction_system.dtos.responses.wallet.WalletResponse;
import com.second_hand_auction_system.models.Transaction;
import com.second_hand_auction_system.models.User;
import com.second_hand_auction_system.models.Wallet;
import com.second_hand_auction_system.repositories.TransactionRepository;
import com.second_hand_auction_system.repositories.UserRepository;
import com.second_hand_auction_system.repositories.WalletRepository;
import com.second_hand_auction_system.service.VNPay.VNPAYService;
import com.second_hand_auction_system.service.email.EmailService;
import com.second_hand_auction_system.service.jwt.IJwtService;
import com.second_hand_auction_system.utils.StatusWallet;
import com.second_hand_auction_system.utils.WalletType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import vn.payos.PayOS;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class WalletService implements IWalletService {
    private final WalletRepository walletRepository;
    private final PayOS payOS;
    private final IJwtService jwtService;
    private final UserRepository userRepository;
    //    private final TransactionWalletRepository transactionWalletRepository;
    private final TransactionRepository transactionRepository;
    private final EmailService emailService;
    private final VNPAYService vnpayService;

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> depositWallet(Deposit deposit) {
        try {
            String authHeader = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
                    .getRequest().getHeader("Authorization");

            // Kiểm tra tiêu đề Authorization
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ResponseObject.builder()
                                .status(HttpStatus.UNAUTHORIZED)
                                .message("Missing or invalid Authorization header")
                                .build());
            }
            // Trích xuất email người dùng từ token
            String token = authHeader.substring(7);
            String userEmail = jwtService.extractUserEmail(token);
            User requester = userRepository.findByEmailAndStatusIsTrue(userEmail).orElse(null);
            if (requester == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ResponseObject.builder()
                                .status(HttpStatus.NOT_FOUND)
                                .message("Unauthorized request - User not found")
                                .build());
            }
            // Gọi dịch vụ VNPay để thực hiện giao dịch
            WalletResponse vnpay = vnpayService.deposite(deposit.getAmount(), deposit.getDescription());


            // Trả về kết quả
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ResponseObject.builder()
                            .status(HttpStatus.OK)
                            .message("Successfully deposited")
                            .data(vnpay)
                            .build());

        } catch (Exception e) {
            // Xử lý lỗi chung
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseObject.builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .message("Transaction failed: " + e.getMessage())
                            .data(null)
                            .build());
        }
    }



    // Phương thức để tạo hoặc lấy ví của người dùng
    private Wallet getOrCreateWallet(User requester) {
        return walletRepository.findByUserId(requester.getId())
                .orElseGet(() -> {
                    Wallet wallet = Wallet.builder()
                            .statusWallet(StatusWallet.ACTIVE)
                            .user(requester)
                            .build();
                    return walletRepository.save(wallet);
                });
    }


//    @Override
//    public ResponseEntity<ResponseObject> getWalletCustomer(Long id) {
//        ObjectMapper objectMapper = new ObjectMapper();
//        ObjectNode response = objectMapper.createObjectNode();
//
//        try {
//            // Lấy thông tin liên kết thanh toán
//            PaymentLinkData order = payOS.getPaymentLinkInformation(id);
//            String authHeader = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
//                    .getRequest().getHeader("Authorization");
//
//            // Kiểm tra tiêu đề Authorization
//            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                        .body(ResponseObject.builder()
//                                .status(HttpStatus.UNAUTHORIZED)
//                                .message("Thiếu hoặc không hợp lệ tiêu đề Authorization")
//                                .build());
//            }
//
//            // Trích xuất token và email người dùng
//            String token = authHeader.substring(7);
//            String userEmail = jwtService.extractUserEmail(token);
//            User requester = userRepository.findByEmailAndStatusIsTrue(userEmail).orElse(null);
//
//            if (requester == null) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
//                        .status(HttpStatus.NOT_FOUND)
//                        .message("Yêu cầu không hợp lệ - Người dùng không tìm thấy")
//                        .data(null)
//                        .build());
//            }
//
//            // Lấy ví giao dịch
//            var transactionWallet = transactionWalletRepository.findTransactionWalletByTransactionWalletCode(id).orElse(null);
//            if (transactionWallet == null) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
//                        .status(HttpStatus.NOT_FOUND)
//                        .message("Không tìm thấy ví giao dịch")
//                        .data(null)
//                        .build());
//            }
//
//            // Lấy ví khách hàng
//            Wallet wallet = walletCustomerRepository.findWalletCustomerByUser_Id(requester.getId()).orElse(null);
//            if (wallet == null) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseObject("Không tìm thấy ví", HttpStatus.NOT_FOUND, response));
//            }
//
//            // Kiểm tra xem giao dịch đã được xử lý chưa
//            if (transactionWallet.getTransactionStatus() == TransactionStatus.COMPLETED) {
//                response.put("error", 4);
//                response.put("message", "Giao dịch đã được xử lý.");
//                response.set("data", objectMapper.valueToTree(order));
//                return ResponseEntity.ok(new ResponseObject("Giao dịch đã được ghi có", HttpStatus.OK, response));
//            }
//
//            // Xử lý trạng thái đơn hàng
//            String orderStatus = order.getStatus();
//            switch (orderStatus) {
//                case "PAID":
//                    // Cập nhật số dư ví
//                    wallet.setBalance(wallet.getBalance() + order.getAmount());
//                    walletCustomerRepository.save(wallet);
//
//                    // Cập nhật chi tiết giao dịch
//                    transactionWallet.setTransactionType(TransactionType.DEPOSIT);
//                    transactionWallet.setTransactionStatus(TransactionStatus.COMPLETED);
//                    transactionWallet.setWallet(wallet);
//                    transactionWalletRepository.save(transactionWallet);
//
//                    response.set("data", objectMapper.valueToTree(order)); // Sử dụng đơn hàng đã nhận
//                    response.put("error", 0);
//                    response.put("message", "Giao dịch thành công. Số tiền đã được ghi có vào ví.");
//                    break;
//
//                case "PENDING":
//                    response.put("error", 1);
//                    response.put("message", "Giao dịch đang chờ. Không thực hiện thêm hành động nào.");
//                    break;
//
//                case "PROCESSING":
//                    response.put("error", 2);
//                    response.put("message", "Giao dịch đang được xử lý. Vui lòng chờ.");
//                    break;
//
//                case "CANCELLED":
//                    transactionWallet.setTransactionStatus(TransactionStatus.CANCELLED);
//                    transactionWalletRepository.save(transactionWallet);
//                    response.put("error", 3);
//                    response.put("message", "Giao dịch đã bị hủy.");
//                    break;
//
//                default:
//                    response.put("error", -1);
//                    response.put("message", "Trạng thái giao dịch không xác định.");
//                    break;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            response.put("error", -1);
//            response.put("message", e.getMessage());
//            response.set("data", null);
//            return ResponseEntity.ok().body(new ResponseObject("Giao dịch thất bại", HttpStatus.BAD_REQUEST, response));
//        }
//
//        // Trả về phản hồi cho việc xử lý giao dịch thành công
//        return ResponseEntity.ok(new ResponseObject("Giao dịch đã được xử lý", HttpStatus.OK, response));
//    }

    @Override
    public ResponseEntity<ResponseObject> getWalletCustomerBalance() {
        String authHeader = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
                .getRequest().getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ResponseObject.builder()
                            .status(HttpStatus.UNAUTHORIZED)
                            .message("Missing or invalid Authorization header")
                            .build());
        }

        String token = authHeader.substring(7);
        String userEmail = jwtService.extractUserEmail(token);
        User requester = userRepository.findByEmailAndStatusIsTrue(userEmail).orElse(null);

        if (requester == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ResponseObject.builder()
                            .status(HttpStatus.NOT_FOUND)
                            .message(" User not found")
                            .build());
        }
        var wallet = walletRepository.findWalletByUserId(requester.getId()).orElse(null);
        if (wallet == null) {
            wallet = Wallet.builder()
                    .user(requester)
                    .balance(0.0)
                    .walletId(requester.getId())
                    .walletType(WalletType.CUSTOMER)
                    .statusWallet(StatusWallet.ACTIVE)
                    .user(requester)
                    .build();
            walletRepository.save(wallet);
            return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder()
                    .status(HttpStatus.OK)
                    .data(wallet.getBalance() )
                    .message("Get balance successfully")
                    .build());
        }
        Double walletCustomer = walletRepository.findBalanceByUserId(requester.getId());
        return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder()
                .status(HttpStatus.OK)
                .data(wallet.getBalance() )
                .message("Get balance successfully")
                .build());


    }
}
