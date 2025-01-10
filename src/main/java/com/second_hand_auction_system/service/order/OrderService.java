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
        if (!auction.getStatus().equals(AuctionStatus.AWAITING_PAYMENT)) {
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

        // Kiểm tra số dư ví của người dùng
        Wallet customerWallet = walletRepository.findWalletByUserId(requester.getId()).orElse(null);
        if (customerWallet == null || customerWallet.getBalance() < winningBid.getBidAmount()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseObject.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Số dư tài khoản của bạn không đủ. Vui lòng nạp thêm để thanh toán đơn hàng")
                    .data(null)
                    .build());
        }

        // Tạo đối tượng order
        Order orderEntity = Order.builder()
                .totalAmount(winningBid.getBidAmount())
                .fullName(order.getFullName())
                .email(order.getEmail())
                .phoneNumber(order.getPhoneNumber())
                .paymentMethod(order.getPaymentMethod())
                .note(order.getNote())
                .createBy(requester.getFullName())
                .status(OrderStatus.ready_to_pick)
                .address(address.getStreet_address())
                .item(auction.getItem())
                .user(requester)
                .shippingMethod("free shipping")
                .auction(auction)
                .orderCode(order.getOrderCode())
                .paymentMethod(order.getPaymentMethod())
                .build();
        orderRepository.save(orderEntity);

//        // Cập nhật số dư ví của khách hàng
//        double oldBalance = customerWallet.getBalance(); // Lưu số dư trước giao dịch
//        double newBalance = oldBalance - winningBid.getBidAmount();
//        customerWallet.setBalance(newBalance);
//        walletRepository.save(customerWallet);

        // Cập nhật và tạo giao dịch cho ví khách hàng
        createCustomerTransaction(customerWallet, winningBid.getBidAmount(), orderEntity, requester);

        // Cập nhật và tạo giao dịch cho ví admin
        createAdminTransaction(winningBid.getBidAmount(), orderEntity);

        return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder()
                .data("Success")
                .message("Order created successfully")
                .status(HttpStatus.OK)
                .build());
    }

    private void createCustomerTransaction(Wallet customerWallet, double bidAmount, Order orderEntity, User requester) {
        double oldBalance = customerWallet.getBalance();
        double newBalance = oldBalance - bidAmount;
        customerWallet.setBalance(newBalance);
        walletRepository.save(customerWallet);

        // Tạo giao dịch cho ví khách hàng
        Transaction transactionWallet = new Transaction();
        transactionWallet.setAmount(-(long) bidAmount);
        transactionWallet.setWallet(customerWallet);
        transactionWallet.setOldAmount((long) oldBalance);
        transactionWallet.setNetAmount((long) newBalance);
        transactionWallet.setTransactionStatus(TransactionStatus.COMPLETED);
        transactionWallet.setTransactionType(TransactionType.TRANSFER);
        transactionWallet.setCommissionAmount(0);
        transactionWallet.setCommissionRate(0);
        transactionWallet.setOrder(orderEntity);
        transactionWallet.setRecipient("Admin");
        transactionWallet.setSender(requester.getFullName());
        transactionWallet.setDescription(orderEntity.getNote());
        transactionWallet.setTransactionWalletCode((random()));
        transactionSystemRepository.save(transactionWallet);
    }
//
    private void createAdminTransaction(double bidAmount, Order orderEntity) {
        Wallet adminWallet = walletRepository.findWalletByWalletType(WalletType.ADMIN).orElse(null);
        if (adminWallet == null) {
            throw new RuntimeException("Admin wallet not found");
        }

        double adminOldBalance = adminWallet.getBalance();
        double adminNewBalance = adminOldBalance + bidAmount;
        adminWallet.setBalance(adminNewBalance);
        walletRepository.save(adminWallet);

//        // Tạo giao dịch cho ví admin
        Transaction transactionAdminWallet = Transaction.builder()
                .amount((long) bidAmount)
                .wallet(adminWallet)
                .oldAmount((long) adminOldBalance)
                .netAmount((long) adminNewBalance)
                .transactionStatus(TransactionStatus.COMPLETED)
                .transactionType(TransactionType.TRANSFER)
                .commissionAmount(0)
                .commissionRate(0)
                .order(orderEntity)
                .recipient(adminWallet.getUser().getFullName())
                .sender("Nguoi thang")
                .description("Payment received for auction")
                .transactionWalletCode((random2()))
                .build();

        transactionSystemRepository.save(transactionAdminWallet);
    }

    private long random() {
        Random random = new Random();
        long transactionCode;
        do {
            // Generate random 6-digit code
            int number = random.nextInt(900000) + 100000;
            transactionCode = Long.parseLong(String.valueOf(number));
        } while (transactionSystemRepository.existsByTransactionWalletCode(transactionCode)); // Check for uniqueness

        return transactionCode;
    }

//    private String random() {
//        return UUID.randomUUID().toString();
//    }

    private long random2() {
        Random random = new Random();
        long transactionCode;
        do {
            // Generate random 6-digit code
            int number = random.nextInt(900000) + 100000;
            transactionCode = Long.parseLong(String.valueOf(number));
        } while (transactionSystemRepository.existsByTransactionWalletCode(transactionCode)); // Check for uniqueness

        return transactionCode;
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
//                    FeedBack feedback = feedbackRepository.findByOrder_OrderId(order.getOrderId());
//                    if (feedback != null) {
//                        response.setFeedback(FeedbackConverter.convertToResponse(feedback));
//                    } else {
//                        response.setFeedback(null);
//                    }

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
    @Scheduled(fixedRate = 60000) //1 phút
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

                    if (ghnStatus.toLowerCase().equals("delivered")) {

                        processPayment(order);

                    }
                }
            } catch (Exception e) {
                System.err.println("Error updating order " + order.getOrderId() + ": " + e.getMessage());
            }
        }
    }

    @Transactional
    public void processPayment(Order order) {
        // Kiểm tra nếu đã có giao dịch với description "Thanh toán tiền cho seller" và orderId hiện tại
        boolean transactionExists = transactionSystemRepository.existsByOrderAndDescription(order, "Thanh toán tiền cho seller");

        if (transactionExists) {
            System.out.println("Transaction for order " + order.getOrderId() + " already exists. Skipping payment process.");
            return;  // Nếu đã tồn tại giao dịch, không thực hiện processPayment
        }

        // Lấy ví của seller
        Wallet sellerWallet = walletRepository.findByUser(order.getItem().getUser())
                .orElseThrow(() -> new RuntimeException("Seller wallet not found for order ID: "
                        + order.getOrderId() + ", Item ID: " + order.getItem().getItemId()
                        + ", Seller User ID: " + order.getItem().getUser().getId()));

        // Lấy ví hệ thống
        Wallet systemWallet = walletRepository.findByWalletType(WalletType.ADMIN)
                .orElseThrow(() -> new RuntimeException("System wallet not found"));

        // Tính toán hoa hồng và số tiền
        double totalAmount = order.getTotalAmount();
        double commissionRate = 0.1; // Ví dụ: 10% hoa hồng
        double commissionAmount = totalAmount * commissionRate;
        double netAmount = totalAmount - commissionAmount;

        double oldBalanceSeller = sellerWallet.getBalance();
        // Cập nhật số dư ví
        sellerWallet.setBalance(sellerWallet.getBalance() + netAmount);
        systemWallet.setBalance(systemWallet.getBalance() + commissionAmount);

        walletRepository.save(sellerWallet);
        walletRepository.save(systemWallet);

        // Lưu giao dịch
        Transaction transaction = new Transaction();
        transaction.setOrder(order);
        transaction.setAmount((long) totalAmount);
        transaction.setCommissionRate(commissionRate);
        transaction.setDescription("Thanh toán tiền cho seller");
        transaction.setCommissionAmount((int) commissionAmount);
        transaction.setNetAmount(netAmount);
        transaction.setOldAmount(oldBalanceSeller);
        transaction.setRecipient(sellerWallet.getUser().getFullName());
        transaction.setSender("System");
        transaction.setTransactionWalletCode(922187);
        transaction.setTransactionType(TransactionType.TRANSFER);
        transaction.setTransactionStatus(TransactionStatus.COMPLETED);
        transaction.setWallet(order.getItem().getUser().getWallet());

        try {
            transactionSystemRepository.save(transaction);
            System.out.println("Transaction saved successfully for order " + order.getOrderId() + transaction);
        } catch (Exception e) {
            System.err.println("Error saving transaction for order " + order.getOrderId() + ": " + e.getMessage());
            e.printStackTrace();  // In chi tiết lỗi ra console
        }

        orderRepository.save(order);

        System.out.println("Processed payment for order " + order.getOrderId());
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
