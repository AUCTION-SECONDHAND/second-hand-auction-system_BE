package com.second_hand_auction_system.service.order;

import com.second_hand_auction_system.dtos.request.order.OrderDTO;
import com.second_hand_auction_system.dtos.responses.ResponseObject;
import com.second_hand_auction_system.dtos.responses.auction.AuctionOrder;
import com.second_hand_auction_system.dtos.responses.item.AuctionItemResponse;
import com.second_hand_auction_system.dtos.responses.item.ItemBriefResponseOrder;
import com.second_hand_auction_system.dtos.responses.order.OrderResponse;
import com.second_hand_auction_system.dtos.responses.user.ListUserResponse;
import com.second_hand_auction_system.models.*;
import com.second_hand_auction_system.repositories.*;
import com.second_hand_auction_system.service.VNPay.VNPAYService;
import com.second_hand_auction_system.service.bid.BidService;
import com.second_hand_auction_system.service.jwt.JwtService;
import com.second_hand_auction_system.utils.*;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import vn.payos.PayOS;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService implements IOrderService {
    private final OrderRepository orderRepository;
    private final AuctionRepository auctionRepository;
    private final BidService bidService;

    private final TransactionRepository transactionSystemRepository;
    private final AddressRepository addressRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;


    @Override
    public ResponseEntity<?> create(OrderDTO order) {
        // Kiểm tra xem phiên đấu giá có tồn tại hay không
        var auction = auctionRepository.findById(order.getAuctionId()).orElse(null);
        if (auction == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                    .data(null)
                    .message("Auction not found")
                    .status(HttpStatus.NOT_FOUND)
                    .build());
        }

        // Kiểm tra trạng thái đấu giá
        if (!auction.getStatus().equals(AuctionStatus.CLOSED)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseObject.builder()
                    .data(null)
                    .message("Auction is not completed")
                    .status(HttpStatus.BAD_REQUEST)
                    .build());
        }

        // Kiểm tra thông tin người yêu cầu từ header Authorization
        String authHeader = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
                .getRequest().getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseObject.builder()
                    .data(null)
                    .message("Missing or invalid Authorization header")
                    .status(HttpStatus.BAD_REQUEST)
                    .build());
        }
        String token = authHeader.substring(7);
        String userEmail = jwtService.extractUserEmail(token);
        var requester = userRepository.findByEmailAndStatusIsTrue(userEmail).orElse(null);
        if (requester == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResponseObject.builder()
                    .data(null)
                    .message("Unauthorized request - User not found")
                    .status(HttpStatus.UNAUTHORIZED)
                    .build());
        }

        // Kiểm tra nếu người dùng đã đặt đơn hàng cho phiên đấu giá này
        var existingOrder = orderRepository.existsByAuction_AuctionIdAndUserId(order.getAuctionId(), requester.getId());
        if (existingOrder) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseObject.builder()
                    .data(null)
                    .message("Đơn hàng đã tồn tại")
                    .status(HttpStatus.BAD_REQUEST)
                    .build());
        }

        // Tìm người thắng đấu giá
        Bid winningBid = bidService.findWinner(auction.getAuctionId());
        if (winningBid == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                    .data(null)
                    .message("No winning bid found for this auction")
                    .status(HttpStatus.NOT_FOUND)
                    .build());
        }
        Address address = addressRepository.findByUserIdAndStatusIsTrue(requester.getId()).orElse(null);
        assert address != null;

        // Tạo đối tượng order
        Order orderEntity = Order.builder()
                .totalAmount(winningBid.getBidAmount()) // Cập nhật giá trị ban đầu
                .fullName(order.getFullName())
                .email(order.getEmail())
                .phoneNumber(order.getPhoneNumber())
                .paymentMethod(order.getPaymentMethod())
                .note(order.getNote())
                .createBy(requester.getFullName())
                .status(OrderStatus.PENDING)
                .address(address.getAddress_name())
                .item(auction.getItem())
                .user(requester)
                .shippingMethod("free shipping")
                .auction(auction)
                .build();

        // Xử lý ví nếu paymentMethod là WALLET_PAYMENT
        if (order.getPaymentMethod().equals(PaymentMethod.WALLET_PAYMENT)) {
            Wallet customerWallet = walletRepository.findWalletByUserId(requester.getId()).orElse(null);

            if (customerWallet == null || customerWallet.getBalance() < orderEntity.getTotalAmount()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseObject.builder()
                        .status(HttpStatus.BAD_REQUEST)
                        .message("Wallet doesn't have enough balance")
                        .data(null));
            }

            log.info("Wallet balance before deduction: " + customerWallet.getBalance());

            // Tính toán hoa hồng và số tiền thực nhận cho admin
            double orderAmount = orderEntity.getTotalAmount();
            double commissionRate = 0.05;
            double commissionAmount = orderAmount * commissionRate;
            double netAmountForAdmin = orderAmount - commissionAmount;

            // Trừ số tiền từ ví customer
            customerWallet.setBalance(customerWallet.getBalance() - orderAmount);
            walletRepository.save(customerWallet);
            log.info("Wallet balance after deduction: " + customerWallet.getBalance());

            // Cập nhật orderEntity
            orderEntity.setTotalAmount(orderAmount + commissionAmount); // Cập nhật tổng tiền bao gồm hoa hồng
            orderRepository.save(orderEntity);

            // Cộng tiền vào ví admin
            Wallet adminWallet = walletRepository.findWalletByWalletType(WalletType.ADMIN).orElse(null);
            if (adminWallet != null) {
                log.info("Admin wallet balance before: " + adminWallet.getBalance());
                adminWallet.setBalance(adminWallet.getBalance() + netAmountForAdmin);
                walletRepository.save(adminWallet);
                log.info("Admin wallet balance after: " + adminWallet.getBalance());
            }

            // Tạo giao dịch ghi nhận giao dịch và hoa hồng
            Transaction transactionWallet = new Transaction();
            transactionWallet.setAmount((long) (orderAmount + netAmountForAdmin));
            transactionWallet.setWallet(customerWallet);
            transactionWallet.setTransactionStatus(TransactionStatus.COMPLETED);
            transactionWallet.setTransactionType(TransactionType.TRANSFER);
            transactionWallet.setCommissionAmount((int) commissionAmount);
            transactionWallet.setCommissionRate(commissionRate);
            transactionWallet.setOrder(orderEntity);
            assert adminWallet != null;
            transactionWallet.setRecipient(adminWallet.getUser().getFullName());
            transactionWallet.setSender(requester.getFullName());
            transactionWallet.setDescription(order.getNote());
            transactionWallet.setTransactionWalletCode(random());
            transactionSystemRepository.save(transactionWallet);

            // Cập nhật trạng thái phiên đấu giá sau khi thanh toán thành công
            auction.setStatus(AuctionStatus.COMPLETED);
            auctionRepository.save(auction);

            return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder()
                    .data("Success")
                    .message("Order created successfully")
                    .status(HttpStatus.OK)
                    .build());
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseObject.builder()
                .data(null)
                .message("Create order failed")
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build());
    }



    private long random() {
        Random random = new Random();
        int number = random.nextInt(900000) + 100000;
        return Long.parseLong(String.valueOf(number));
    }


    @Override
    public ResponseEntity<?> getOrders(Integer page, Integer size, String sortBy, OrderStatus status) {
        if (page == null) {
            page = 0;
        }
        if (size == null) {
            size = 10;
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        Page<Order> orders;
        if (status != null) {
            orders = orderRepository.findByStatus(status, pageable);
        }
        else{
            orders = orderRepository.findAll(pageable);
        }

        List<OrderResponse> orderResponses = orders.stream()
                .map(order -> {
                    OrderResponse response = new OrderResponse();
                    response.setOrderId(order.getOrderId());
                    response.setOrderStatus(order.getStatus()); // Nếu OrderStatus là enum
                    response.setPaymentMethod(order.getPaymentMethod()); // Nếu PaymentMethod là enum
                    response.setEmail(order.getEmail());
                    response.setPhoneNumber(order.getPhoneNumber());
                    response.setNote(order.getNote());
                    Item item = order.getItem(); // Giả sử order.getItem() trả về Item
                    if (item != null) {
                        response.setItem(ItemBriefResponseOrder.builder()
                                .itemId(item.getItemId())
                                .itemName(item.getItemName())
                                .thumbnail(item.getThumbnail())
                                .sellerName(item.getUser() != null ? item.getCreateBy() : null)
                                .build());
                    }
                    Auction auction = order.getAuction();
                    if (auction != null) {
                        response.setAuctionOrder(AuctionOrder.builder()
                                .auctionId(auction.getAuctionId())
                                .termConditions(auction.getTermConditions())
                                .auctionTypeName(auction.getAuctionType().getAuctionTypeName())
                                .priceStep(auction.getPriceStep())
                                .status(auction.getStatus())
                                .build());
                    }
                    response.setCreateBy(order.getCreateBy());
                    response.setTotalPrice(order.getTotalAmount());
                    response.setShippingType(order.getShippingMethod());
                    return response;
                })
                .collect(Collectors.toList());
        Map<String,Object> response = new HashMap<>();
        response.put("orders", orderResponses);
        response.put("totalPage", orders.getTotalPages());
        response.put("totalElements", orders.getTotalElements());
        return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder()
                .data(response)
                .message("Orders found")
                .status(HttpStatus.OK)
                .build());
    }

    @Override
    public ResponseEntity<?> getOrderByUser(int size, int page) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createAt"));
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
        var requester = userRepository.findByEmailAndStatusIsTrue(userEmail).orElse(null);

        if (requester == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ResponseObject.builder()
                            .status(HttpStatus.NOT_FOUND)
                            .message("User not found")
                            .data(null)
                            .build());
        }

        Page<Order> orders = orderRepository.findAllByUser_Id(requester.getId(), pageable);
        List<OrderResponse> orderResponses = orders.stream()
                .map(order -> {
                    OrderResponse response = new OrderResponse();
                    response.setOrderId(order.getOrderId());
                    response.setOrderStatus(order.getStatus()); // Assuming OrderStatus is enum
                    response.setPaymentMethod(order.getPaymentMethod()); // Assuming PaymentMethod is enum
                    response.setEmail(order.getEmail());
                    response.setPhoneNumber(order.getPhoneNumber());
                    response.setNote(order.getNote());
                    Item item = order.getItem(); // Giả sử order.getItem() trả về Item
                    if (item != null) {
                        response.setItem(ItemBriefResponseOrder.builder()
                                .itemId(item.getItemId())
                                .itemName(item.getItemName())
                                .thumbnail(item.getThumbnail())
                                .sellerName(item.getUser() != null ? item.getCreateBy() : null)
                                .build());
                    }

                    Auction auction = order.getAuction();
                    if (auction != null) {
                        response.setAuctionOrder(AuctionOrder.builder()
                                .auctionId(auction.getAuctionId())
                                .termConditions(auction.getTermConditions())
                                .auctionTypeName(auction.getAuctionType().getAuctionTypeName())
                                .priceStep(auction.getPriceStep())
                                .status(auction.getStatus())
                                .build());
                    }
                    response.setCreateBy(order.getCreateBy());
                    response.setTotalPrice(order.getTotalAmount());
                    response.setShippingType(order.getShippingMethod());
                    return response;
                })
                .toList();

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseObject.builder()
                        .status(HttpStatus.OK)
                        .message("List of orders found")
                        .data(orderResponses)  // Pass the processed list
                        .build());
    }


}
