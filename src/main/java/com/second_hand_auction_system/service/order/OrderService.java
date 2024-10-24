package com.second_hand_auction_system.service.order;

import com.second_hand_auction_system.configurations.VNPayConfig;
import com.second_hand_auction_system.dtos.request.order.OrderDTO;
import com.second_hand_auction_system.dtos.responses.ResponseObject;
import com.second_hand_auction_system.dtos.responses.order.OrderResponse;
import com.second_hand_auction_system.dtos.responses.order.VnpayResponse;
import com.second_hand_auction_system.models.Bid;
import com.second_hand_auction_system.models.Item;
import com.second_hand_auction_system.models.Order;
import com.second_hand_auction_system.repositories.AuctionRepository;
import com.second_hand_auction_system.repositories.ItemRepository;
import com.second_hand_auction_system.repositories.OrderRepository;
import com.second_hand_auction_system.service.VNPay.VNPAYService;
import com.second_hand_auction_system.service.bid.BidService;
import com.second_hand_auction_system.utils.AuctionStatus;
import com.second_hand_auction_system.utils.OrderStatus;
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

import java.util.List;
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
    @Override
    @Transactional
    public ResponseEntity<?> create(OrderDTO order,HttpServletRequest request) {
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
        if (!auction.getStatus().equals(AuctionStatus.COMPLETED)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseObject.builder()
                    .data(null)
                    .message("Auction is not completed")
                    .status(HttpStatus.BAD_REQUEST)
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
                .shippingMethod("free shipping")
                .auction(auction)
                .build();
        orderRepository.save(orderEntity);
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        ResponseEntity<?> vnpayResponse = vnpayService.paymentOrder(winningBid.getBidAmount(), orderEntity.getOrderId(), baseUrl);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseObject.builder()
                .data(vnpayResponse)
                .message("Order created successfully")
                .status(HttpStatus.CREATED)
                .build());
    }

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
                    response.setOrderStatus(order.getStatus()); // Nếu OrderStatus là enum
                    response.setPaymentMethod(order.getPaymentMethod()); // Nếu PaymentMethod là enum
                    response.setEmail(order.getEmail());
                    response.setPhoneNumber(order.getPhoneNumber());
                    response.setQuantity(order.getQuantity());
                    response.setNote(order.getNote());
                    response.setItemId(order.getItem() != null ? order.getItem().getItemId() : null);
                    response.setAuctionId(order.getAuction() != null ? order.getAuction().getAuctionId() : null);
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


}
