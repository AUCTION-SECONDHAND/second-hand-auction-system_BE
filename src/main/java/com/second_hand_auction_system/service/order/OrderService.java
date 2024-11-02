package com.second_hand_auction_system.service.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.second_hand_auction_system.dtos.request.order.OrderDTO;
import com.second_hand_auction_system.dtos.responses.ResponseObject;
import com.second_hand_auction_system.dtos.responses.auction.AuctionOrder;
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
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;
import vn.payos.type.PaymentData;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {
    private final OrderRepository orderRepository;
    private final AuctionRepository auctionRepository;
    private final ItemRepository itemRepository;
    private final BidService bidService;
    private final ModelMapper modelMapper;
    private final VNPAYService vnpayService;
//    private final TransactionSystemRepository transactionSystemRepository;
    private final PayOS payOS;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final WalletCustomerRepository walletCustomerRepository;
//    private final WalletSystemRepository walletSystemRepository;
//    private final TransactionWalletRepository transactionWalletRepository;

    @Override
    @Transactional
    public ResponseEntity<?> create(OrderDTO order, HttpServletRequest request) {
        Item item = itemRepository.findByAuction_AuctionId(order.getAuction());
        if (item == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                    .data(null)
                    .message("Item not found")
                    .status(HttpStatus.NOT_FOUND)
                    .build());
        }
        var auction = auctionRepository.findById(order.getAuction()).orElse(null);
        if (auction == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                    .data(null)
                    .message("Auction not found")
                    .status(HttpStatus.NOT_FOUND)
                    .build());
        }
        if (!(auction.getStatus().equals(AuctionStatus.COMPLETED) || auction.getStatus().equals(AuctionStatus.CLOSED))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseObject.builder()
                    .data(null)
                    .message("Auction is not completed")
                    .status(HttpStatus.BAD_REQUEST)
                    .build());
        }
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
        Bid winningBid = bidService.findWinner(auction.getAuctionId());
        if (winningBid == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                    .data(null)
                    .message("No winning bid found for this auction")
                    .status(HttpStatus.NOT_FOUND)
                    .build());
        }
        Order orderEntity = Order.builder()
                .totalAmount(winningBid.getBidAmount())
                .email(order.getEmail())
                .quantity(order.getQuantity())
                .phoneNumber(order.getPhoneNumber())
                .paymentMethod(order.getPaymentMethod())
                .note(order.getNote())
                .createBy(order.getCreateBy())
                .status(OrderStatus.PENDING)
                .item(item)
                .user(requester)
                .shippingMethod("free shipping")
                .auction(auction)
                .build();
        orderRepository.save(orderEntity);
        if (order.getPaymentMethod().equals(PaymentMethod.VN_PAYMENT)) {
            String baseUrl = order.getReturnSuccess();
            ResponseEntity<?> vnpayResponse = vnpayService.paymentOrder(winningBid.getBidAmount(), orderEntity.getOrderId(), baseUrl);
            return ResponseEntity.status(HttpStatus.CREATED).body(ResponseObject.builder()
                    .data(vnpayResponse)
                    .message("Order created successfully")
                    .status(HttpStatus.CREATED)
                    .build());
        }
//        if (order.getPaymentMethod().equals(PaymentMethod.PAY_OS)) {
//            String successUrl = "https://your-success-url.com"; // Replace with your actual success URL
//            String cancelUrl = "https://www.facebook.com/minhskn"; // Replace with your actual cancel URL
//            order.setFailureUrl(cancelUrl);
//            order.setReturnSuccess(successUrl);
//            String currentTime = String.valueOf(new Date().getTime());
//            long depositCode = Long.parseLong(currentTime.substring(currentTime.length() - 6));
//            TransactionType transactionType = TransactionType.builder()
//                    .order(orderEntity)
//                    .transactionType(TransactionType.TRANSFER)
////                    .transactionSystemCode(paymentData.getOrderCode().toString())
//                    .user(orderEntity.getItem().getUser())
//                    .description(order.getNote())
//                    .amount(winningBid.getBidAmount())
//                    .status(TransactionStatus.PENDING)
//                    .transactionTime(currentTime)
//                    .build();
//            transactionSystemRepository.save(transactionType);
//            return createOrderByPayOS(order);
//        }
        if (order.getPaymentMethod().equals(PaymentMethod.WALLET_PAYMENT)) {
//            return paymentByWallet(requester.getId(), (int) orderEntity.getTotalAmount());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseObject.builder()
                .data(null)
                .message("Order created successfully")
                .status(HttpStatus.CREATED)
                .build());
    }

//    private ResponseEntity<?> paymentByWallet(Integer userId, int amount) {
//        Wallet wallet = walletCustomerRepository.findWalletCustomerByUser_Id(userId).orElse(null);
//        if (wallet == null || wallet.getBalance() < amount) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseObject.builder()
//                    .status(HttpStatus.BAD_REQUEST)
//                    .message("Wallet don't have enough balance")
//                    .data(null));
//        }
//        var user = userRepository.findById(userId).orElse(null);
//        if (user == null) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
//                    .status(HttpStatus.NOT_FOUND)
//                    .message("User not found")
//                    .data(null));
//        }
//        WalletType walletType = walletSystemRepository.findFirstByOrderByWalletAdminIdAsc().orElse(null);
//        TransactionWallet transactionWallet = new TransactionWallet();
//        transactionWallet.setAmount((long) (wallet.getBalance() - amount));
//        transactionWallet.setWallet(wallet);
//        transactionWallet.setTransactionStatus(TransactionStatus.PENDING);
//        transactionWallet.setTransactionType(com.second_hand_auction_system.utils.TransactionType.TRANSFER);
//        transactionWallet.setCommissionAmount((int) (0.05 * amount));
//        transactionWallet.setCommissionRate(0.05);
//        transactionWallet.setWalletType(walletType);
//        transactionWalletRepository.save(transactionWallet);
//        return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder().data(null).message("Create transactionWallet successfully").status(HttpStatus.OK));
//    }

//    public ResponseEntity<?> createOrderByPayOS(OrderDTO order) {
//        ObjectMapper mapper = new ObjectMapper();
//        ObjectNode response = mapper.createObjectNode();
//
//        try {
//            String authHeader = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
//                    .getRequest().getHeader("Authorization");
//            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                        .body(ResponseObject.builder()
//                                .status(HttpStatus.UNAUTHORIZED)
//                                .message("Missing or invalid Authorization header")
//                                .build());
//            }
//
//            String token = authHeader.substring(7);
//            String userEmail = jwtService.extractUserEmail(token);
//            User requester = userRepository.findByEmailAndStatusIsTrue(userEmail).orElse(null);
//
//            if (requester == null) {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                        .body(ResponseObject.builder()
//                                .status(HttpStatus.UNAUTHORIZED)
//                                .message("Unauthorized request - User not found")
//                                .build());
//            }
//
//            if (requester.getRole() == Role.BUYER || requester.getRole() == Role.SELLER) {
//                Item item = itemRepository.findByAuction_AuctionId(order.getAuction());
//                if (item == null) {
//                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
//                            .data(null)
//                            .message("Item not found")
//                            .status(HttpStatus.NOT_FOUND)
//                            .build());
//                }
//                var auction = auctionRepository.findById(order.getAuction()).orElse(null);
//                if (auction == null) {
//                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
//                            .data(null)
//                            .message("Auction not found")
//                            .status(HttpStatus.NOT_FOUND)
//                            .build());
//                }
//                if (!auction.getStatus().equals(AuctionStatus.COMPLETED)) {
//                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseObject.builder()
//                            .data(null)
//                            .message("Auction is not completed")
//                            .status(HttpStatus.BAD_REQUEST)
//                            .build());
//                }
//
//                Bid winningBid = bidService.findWinner(auction.getAuctionId());
//                if (winningBid == null) {
//                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
//                            .data(null)
//                            .message("No winning bid found for this auction")
//                            .status(HttpStatus.NOT_FOUND)
//                            .build());
//                }
//                Bid winner = bidService.findWinner(auction.getAuctionId());
//
//                double balance = winner.getBidAmount(); // Get the amount to be paid from OrderDTO
//                String description = order.getNote();
//                String successUrl = order.getReturnSuccess(); // Get success URL from OrderDTO
//                String cancelUrl = order.getFailureUrl();
//
//                String currentTime = String.valueOf(new Date().getTime());
//                long depositCode = Long.parseLong(currentTime.substring(currentTime.length() - 6));
//
//                ItemData itemData = ItemData.builder()
//                        .name("Thanh toán đơn hàng #" + item.getItemName())
//                        .price((int) balance)
//                        .quantity(1)
//                        .build();
//
//                PaymentData paymentData = PaymentData.builder()
//                        .orderCode(depositCode)
//                        .description(description)
//                        .amount((int) balance)
//                        .item(itemData)
//                        .returnUrl(successUrl)
//                        .cancelUrl(cancelUrl)
//                        .build();
////                Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
////                SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
////                String vnp_CreateDate = formatter.format(cld.getTime());
//                CheckoutResponseData paymentLinkData = payOS.createPaymentLink(paymentData);
//                var transactionSystem = transactionSystemRepository.findTransactionSystemByUser_Id(requester.getId()).orElse(null);
//                assert transactionSystem != null;
//                transactionSystem.setVirtualAccountName(requester.getFullName());
//                transactionSystem.setTransactionSystemCode(paymentLinkData.getOrderCode().toString());
//                transactionSystemRepository.save(transactionSystem);
//                response.put("error", 0);
//                response.put("message", "Payment link created successfully");
//                response.set("data", mapper.valueToTree(paymentLinkData));
//                return ResponseEntity.ok(new ResponseObject("Payment link created", HttpStatus.OK, paymentLinkData));
//
//            } else {
//                response.put("error", 1);
//                response.put("message", "Unauthorized role for deposit");
//                return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                        .body(new ResponseObject("Unauthorized role", HttpStatus.FORBIDDEN, null));
//            }
//        } catch (Exception e) {
//            response.put("error", -1);
//            response.put("message", "An error occurred: " + e.getMessage());
//            response.set("data", null);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(new ResponseObject("An error occurred", HttpStatus.INTERNAL_SERVER_ERROR, null));
//        }
//    }


    @Override
    public ResponseEntity<?> getOrders(Integer page, Integer size, String sortBy) {
        if (page == null) {
            page = 0;
        }
        if (size == null) {
            size = 10;
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        Page<Order> orders = orderRepository.findAll(pageable);

        List<OrderResponse> orderResponses = orders.stream()
                .map(order -> {
                    OrderResponse response = new OrderResponse();
                    response.setOrderId(order.getOrderId());
                    response.setOrderStatus(order.getStatus()); // Nếu OrderStatus là enum
                    response.setPaymentMethod(order.getPaymentMethod()); // Nếu PaymentMethod là enum
                    response.setEmail(order.getEmail());
                    response.setPhoneNumber(order.getPhoneNumber());
                    response.setQuantity(order.getQuantity());
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

        return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder()
                .data(orderResponses)
                .message("Orders found")
                .status(HttpStatus.OK)
                .build());
    }

    @Override
    public ResponseEntity<?> getOrderByUser(int size, int page) {
        Pageable pageable = PageRequest.of(page, size);
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
                    response.setQuantity(order.getQuantity());
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
