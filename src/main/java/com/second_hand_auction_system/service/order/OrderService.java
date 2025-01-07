package com.second_hand_auction_system.service.order;

import com.second_hand_auction_system.converters.feedback.FeedbackConverter;
import com.second_hand_auction_system.dtos.request.order.OrderDTO;
import com.second_hand_auction_system.dtos.responses.ResponseObject;
import com.second_hand_auction_system.dtos.responses.auction.AuctionOrder;
import com.second_hand_auction_system.dtos.responses.item.ItemBriefResponseOrder;
import com.second_hand_auction_system.dtos.responses.order.GHNResponse;
import com.second_hand_auction_system.dtos.responses.order.OrderDetailResponse;
import com.second_hand_auction_system.dtos.responses.order.OrderResponse;
import com.second_hand_auction_system.dtos.responses.order.SellerOrderStatics;
import com.second_hand_auction_system.dtos.responses.transaction.TransactionResponse;
import com.second_hand_auction_system.models.*;
import com.second_hand_auction_system.repositories.*;
import com.second_hand_auction_system.service.bid.BidService;
import com.second_hand_auction_system.service.ghn.GHNService;
import com.second_hand_auction_system.service.jwt.JwtService;
import com.second_hand_auction_system.utils.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
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
    private final FeedbackRepository feedbackRepository;
    private final com.second_hand_auction_system.converters.order.orderConverter orderConverter;
    private final GHNService ghnService;

    @Override
    @Transactional
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
        if (address == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseObject.builder()
                    .data(null)
                    .message("Address not found")
                    .status(HttpStatus.BAD_REQUEST)
                    .build());
        }

        // Tạo đối tượng order
        Order orderEntity = Order.builder()
                .totalAmount(winningBid.getBidAmount()) // Cập nhật giá trị ban đầu
                .fullName(order.getFullName())
                .email(order.getEmail())
                .phoneNumber(order.getPhoneNumber())
                .paymentMethod(order.getPaymentMethod())
                .note(order.getNote())
                .createBy(requester.getFullName())
                .status(OrderStatus.ready_to_pick)
                .address(address.getAddress_name())
                .item(auction.getItem())
                .user(requester)
                .shippingMethod("free shipping")
                .auction(auction)
                .orderCode(order.getOrderCode())
                .paymentMethod(order.getPaymentMethod())
                .build();
        orderRepository.save(orderEntity);
        // Xử lý ví nếu paymentMethod là WALLET_PAYMENT
        if (order.getPaymentMethod().equals(PaymentMethod.WALLET_PAYMENT)) {
            try {
                Wallet customerWallet = walletRepository.findWalletByUserId(requester.getId()).orElse(null);
                Wallet adminWallet = walletRepository.findWalletByWalletType(WalletType.ADMIN).orElse(null);
                if (customerWallet == null || customerWallet.getBalance() < orderEntity.getTotalAmount()) {
                    log.warn("Insufficient wallet balance or wallet not found for user: " + requester.getId());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseObject.builder()
                            .status(HttpStatus.BAD_REQUEST)
                            .message("Số dư tài khoản của bạn không đủ.Vui lòng nạp thêm để thanh toán đơn hàng")
                            .data(null)
                            .build());
                }

                log.info("Wallet balance before deduction: " + customerWallet.getBalance());

// Tính toán số tiền cần trừ và kiểm tra
                double orderAmount = orderEntity.getTotalAmount();
                if (customerWallet.getBalance() < orderAmount) {
                    log.error("Insufficient balance for transaction");
                }

// Lấy số dư ví hiện tại trước giao dịch
                long oldBalance = (long) customerWallet.getBalance();
                log.info("Wallet balance before deduction: " + oldBalance);

// Kiểm tra nếu số dư không đủ
                if (oldBalance < orderAmount) {
                    log.error("Insufficient balance for transaction");
                }

// Tính số dư sau giao dịch
                long newBalance = oldBalance - (long) orderAmount;

// Cập nhật số dư ví khách hàng
                customerWallet.setBalance(newBalance);
                walletRepository.save(customerWallet);

                // Log số dư sau khi cập nhật
                log.info("Wallet balance after deduction: " + newBalance);

                // Tạo và lưu giao dịch
                Transaction transactionWallet = new Transaction();
                transactionWallet.setAmount(-(long) orderAmount); // Giá trị giao dịch âm
                transactionWallet.setWallet(customerWallet); // Liên kết với ví
                transactionWallet.setOldAmount(oldBalance); // Số dư trước giao dịch
                transactionWallet.setNetAmount(newBalance); // Số dư sau giao dịch
                transactionWallet.setTransactionStatus(TransactionStatus.COMPLETED);
                transactionWallet.setTransactionType(TransactionType.TRANSFER);
                transactionWallet.setCommissionAmount(0);
                transactionWallet.setCommissionRate(0);
                transactionWallet.setOrder(orderEntity);
                transactionWallet.setRecipient(adminWallet != null ? adminWallet.getUser().getFullName() : "Admin");
                transactionWallet.setSender(requester.getFullName());
                transactionWallet.setDescription(order.getNote());
                transactionWallet.setTransactionWalletCode(random());
                transactionSystemRepository.save(transactionWallet);
                log.info("Transaction saved successfully with details: " + transactionWallet);
                // Cập nhật trạng thái phiên đấu giá sau khi thanh toán thành công
                auctionRepository.save(auction);
                // Hoàn tiền cọc cho người thắng đấu giá
                Wallet depositWallet = walletRepository.findWalletByAuctionId(auction.getAuctionId()).orElse(null);
                if (depositWallet != null) {
                    long depositAmount = (long) (auction.getPercentDeposit() * auction.getBuyNowPrice());
                    long oldDepositBalance = (long) depositWallet.getBalance();
                    long newDepositBalance = oldDepositBalance + depositAmount;

                    // Cập nhật số dư ví người thắng đấu giá
                    depositWallet.setBalance(newDepositBalance);
                    walletRepository.save(depositWallet);
                    // Tạo giao dịch hoàn tiền
                    Transaction refundTransaction = new Transaction();
                    refundTransaction.setAmount(depositAmount); // Số tiền hoàn cọc (dương)
                    refundTransaction.setWallet(depositWallet); // Ví của người thắng đấu giá
                    refundTransaction.setOldAmount(oldDepositBalance); // Số dư trước khi hoàn tiền
                    refundTransaction.setNetAmount(newDepositBalance); // Số dư sau khi hoàn tiền
                    refundTransaction.setTransactionStatus(TransactionStatus.COMPLETED);
                    refundTransaction.setTransactionType(TransactionType.REFUND); // Loại giao dịch là hoàn tiền
                    refundTransaction.setDescription("Hoàn tiền cọc sau khi thanh toán đơn hàng thành công");
                    refundTransaction.setSender("Hệ thống");
                    refundTransaction.setRecipient(requester.getFullName());
                    refundTransaction.setTransactionWalletCode(random()); // Mã giao dịch ngẫu nhiên
                    transactionSystemRepository.save(refundTransaction);

                    log.info("Deposit refund transaction saved successfully: " + refundTransaction);
                }

                return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder()
                        .data("Success")
                        .message("Order created successfully")
                        .status(HttpStatus.OK)
                        .build());

            } catch (Exception ex) {
                log.error("Error occurred during wallet payment processing: ", ex);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseObject.builder()
                        .data(null)
                        .message("An unexpected error occurred during wallet payment processing")
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .build());
            }
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
        if (status == null) {
            orders = orderRepository.findAll(pageable);

        } else {
            orders = orderRepository.findByStatus(status, pageable);

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
                    response.setOrderCode(order.getOrderCode());
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
        Map<String, Object> response = new HashMap<>();
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
                    response.setOrderCode(order.getOrderCode());
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

                    // Lấy Feedback
                    FeedBack feedback = feedbackRepository.findByOrder_OrderId(order.getOrderId());
                    if (feedback != null) {
                        response.setFeedback(FeedbackConverter.convertToResponse(feedback));
                    } else {
                        response.setFeedback(null);
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

    @Override
    public ResponseEntity<?> getStatistic() {
        String authHeader = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest().getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResponseObject.builder()
                    .status(HttpStatus.UNAUTHORIZED)
                    .data(null)
                    .message("Missing or invalid Authorization header")
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
        Wallet walletAdmin = walletRepository.findWalletByWalletType(WalletType.ADMIN).orElse(null);
        if (walletAdmin == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                    .status(HttpStatus.NOT_FOUND)
                    .data(null)
                    .message("WalletAdmin not found")
                    .build());
        }
        float balanceAdmin = (float) walletAdmin.getBalance();
        long totalTransaction = transactionSystemRepository.count();
        int totalUser = (int) userRepository.count();
        int totalAuction = (int) auctionRepository.count();
        int totalOrder = (int) orderRepository.count();
        TransactionResponse transactionResponse = TransactionResponse.builder()
                .balance(balanceAdmin)
                .totalTransaction(totalTransaction)
                .totalUser(totalUser)
                .totalAuction(totalAuction)
                .totalOrder(totalOrder)
                .totalRevenue(totalOrder * 0.05)
                .build();


        return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder()
                .status(HttpStatus.OK)
                .data(transactionResponse)
                .message("Revenue data")
                .build());
    }

    @Override
    public ResponseEntity<?> getOrderBySeller(int size, int page) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createAt"));
        String authHeader = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest().getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResponseObject.builder()
                    .status(HttpStatus.UNAUTHORIZED)
                    .message("Missing or invalid Authorization header")
                    .data(null)
                    .build());
        }
        String token = authHeader.substring(7);
        String userEmail = jwtService.extractUserEmail(token);
        var requester = userRepository.findByEmailAndStatusIsTrue(userEmail).orElse(null);
        if (requester == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                    .status(HttpStatus.NOT_FOUND)
                    .message("User not found")
                    .data(null)
                    .build());
        }
        Page<Order> orders = orderRepository.findAllByAuction_Item_User_Id(requester.getId(), pageable);
        List<OrderResponse> orderResponses = orders.stream()
                .map(order -> {
                    OrderResponse response = new OrderResponse();
                    response.setOrderId(order.getOrderId());
                    response.setOrderStatus(order.getStatus()); // Nếu OrderStatus là enum
                    response.setPaymentMethod(order.getPaymentMethod()); // Nếu PaymentMethod là enum
                    response.setEmail(order.getEmail());
                    response.setPhoneNumber(order.getPhoneNumber());
                    response.setNote(order.getNote());
                    response.setOrderCode(order.getOrderCode());
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
        Map<String, Object> response = new HashMap<>();
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
   // @Scheduled(fixedRate = 600000)
    @Scheduled(fixedRate = 600000) //1 phút
    public void updateOrderStatuses() {
        // Retrieve all orders that are pending status updates
        List<Order> ordersToUpdate = orderRepository.findAll(); // Or some other filter condition

        for (Order order : ordersToUpdate) {
            try {
                // Get the GHN status using the order code
                GHNResponse ghnResponse = ghnService.getOrderDetails(order.getOrderCode());

                if (ghnResponse != null && ghnResponse.getCode() == 200) {
                    // Update order status based on GHN response
                    String ghnStatus = ghnResponse.getData().getStatus();
                    order.setStatus(OrderStatus.valueOf(ghnStatus.toLowerCase())); // Adjust status based on your enum
                    orderRepository.save(order);
                    System.out.println("Updated order " + order.getOrderId() + " with status " + ghnStatus);
                }
            } catch (Exception e) {
                System.err.println("Error updating order " + order.getOrderId() + ": " + e.getMessage());
            }
        }
    }

    @Override
    public OrderDetailResponse getOrderDetail(int orderId) {
        Order orderExisted = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return orderConverter.toOrderDetailResponse(orderExisted);
    }

    @Override
    public ResponseEntity<?> getOrderStatisticsByMonth() {
        List<Object[]> statistics = orderRepository.getOrderStatisticsByMonth();
        List<SellerOrderStatics> statisticsDTOs = new ArrayList<>();

        for (Object[] stat : statistics) {
            int month = (int) stat[0];
            long totalOrders = (long) stat[1];
            long deliveredOrders = (long) stat[2];
            long cancelledOrders = (long) stat[3];
            double totalAmount = (double) stat[4];

            SellerOrderStatics dto = new SellerOrderStatics(
                    month, totalOrders, deliveredOrders, cancelledOrders, totalAmount
            );
            statisticsDTOs.add(dto);
        }

        return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder()
                .message("Get order statistics")
                .data(statisticsDTOs)
                .build());
    }


    @Override
    public ResponseEntity<?> getTotalMoney() {
        List<Object[]> statistics = orderRepository.getTotalMoneyByMonth();

        // Tạo một mảng với 12 tháng và giá trị mặc định là 0
        double[] monthlyAmounts = new double[12];

        // Lặp qua các kết quả trả về từ database và cập nhật giá trị cho tháng tương ứng
        for (Object[] stat : statistics) {
            int month = (int) stat[0]; // Lấy tháng
            double totalAmount = (double) stat[1]; // Lấy tổng tiền của tháng đó
            monthlyAmounts[month - 1] = totalAmount; // Gán giá trị cho tháng tương ứng (tháng 1 = index 0)
        }

        // Tạo danh sách các tháng và số tiền
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", i + 1); // Tháng (1-12)
            monthData.put("totalAmount", monthlyAmounts[i]); // Số tiền tổng cho tháng đó
            result.add(monthData);
        }

        return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder()
                .message("Get total money by month")
                .data(result)
                .build());
    }




}
