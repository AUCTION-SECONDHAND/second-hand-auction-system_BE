package com.second_hand_auction_system.service.order;

import com.second_hand_auction_system.dtos.request.order.OrderDTO;
import com.second_hand_auction_system.dtos.responses.ResponseObject;
import com.second_hand_auction_system.dtos.responses.order.OrderResponse;
import com.second_hand_auction_system.models.Bid;
import com.second_hand_auction_system.models.Item;
import com.second_hand_auction_system.models.Order;
import com.second_hand_auction_system.repositories.AuctionRepository;
import com.second_hand_auction_system.repositories.ItemRepository;
import com.second_hand_auction_system.repositories.OrderRepository;
import com.second_hand_auction_system.service.bid.BidService;
import com.second_hand_auction_system.utils.AuctionStatus;
import com.second_hand_auction_system.utils.OrderStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {
    private final OrderRepository orderRepository;
    private final AuctionRepository auctionRepository;
    private final ItemRepository itemRepository;
    private final BidService bidService;
    private final ModelMapper modelMapper;
    @Override
    @Transactional
    public ResponseEntity<?> create(OrderDTO order) {
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
        OrderResponse orderResponse1 = OrderResponse.builder()
                .totalPrice(winningBid.getBidAmount())
                .email(order.getEmail())
                .quantity(order.getQuantity())
                .phoneNumber(order.getPhoneNumber())
                .paymentMethod(order.getPaymentMethod())
                .note(order.getNote())
                .createBy(order.getCreateBy())
                .orderStatus(OrderStatus.PENDING)
                .itemId(item.getItemId())
                .shippingType("free shipping")
                .auctionId(auction.getAuctionId())
                .build();
        orderRepository.save(orderEntity);

        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseObject.builder()
                .data(orderResponse1)
                .message("Order created successfully")
                .status(HttpStatus.CREATED)
                .build());
    }

    @Override
    public ResponseEntity<?> getOrders(String search ,Integer page,Integer pageSize) {
        Pageable pageable = PageRequest.of(page,pageSize);
        Page<Order> orderPage;
        if(!search.isEmpty() ){

        }
        return null;
    }

}
