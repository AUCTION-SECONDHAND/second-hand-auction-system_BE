package com.second_hand_auction_system.service.auctionRegistrations;

import com.second_hand_auction_system.converters.auctionRegistrations.AuctionRegistrationsConverter;
import com.second_hand_auction_system.dtos.request.auctionRegistrations.AuctionRegistrationsDto;
import com.second_hand_auction_system.dtos.responses.auctionRegistrations.AuctionRegistrationsResponse;
import com.second_hand_auction_system.models.Auction;
import com.second_hand_auction_system.models.AuctionRegistration;
import com.second_hand_auction_system.models.User;
import com.second_hand_auction_system.models.WalletCustomer;
import com.second_hand_auction_system.repositories.AuctionRegistrationsRepository;
import com.second_hand_auction_system.repositories.AuctionRepository;
import com.second_hand_auction_system.repositories.UserRepository;
import com.second_hand_auction_system.repositories.WalletCustomerRepository;
import com.second_hand_auction_system.service.jwt.IJwtService;
import com.second_hand_auction_system.service.walletCustomer.WalletCustomerService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuctionRegistrationsService implements IAuctionRegistrationsService {
    private final AuctionRegistrationsRepository auctionRegistrationsRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final AuctionRepository auctionRepository;
    private final WalletCustomerRepository walletCustomerRepository;
    private final WalletCustomerService walletCustomerService;
    private final IJwtService jwtService;
    private final AuctionRegistrationsConverter auctionRegistrationsConverter;

    @Override
    public void addAuctionRegistration(AuctionRegistrationsDto auctionRegistrationsDto) throws Exception {

        String authHeader = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest().getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new Exception("Unauthorized");
        }
        String token = authHeader.substring(7);
        String userEmail = jwtService.extractUserEmail(token);
        User requester = userRepository.findByEmailAndStatusIsTrue(userEmail).orElse(null);
        if (requester == null) {
            throw new Exception("User not found");
        }
        WalletCustomer walletCustomerCheckBalance = walletCustomerRepository.findByUserIdAndBalanceGreaterThanEqual100(requester.getId());
        if (walletCustomerCheckBalance == null) {
            throw new Exception("You do not have enough money in your wallet, please top up");
        }
        Auction auctionExist = auctionRepository.findById(auctionRegistrationsDto.getAuction())
                .orElseThrow(() -> new RuntimeException("Auction not found"));
        AuctionRegistration auctionRegistration = modelMapper.map(auctionRegistrationsDto, AuctionRegistration.class);
        auctionRegistration.setUser(requester);
        auctionRegistration.setAuction(auctionExist);
        auctionRegistrationsRepository.save(auctionRegistration);


    }

    @Override
    public void updateAuctionRegistration(int arId, AuctionRegistrationsDto auctionRegistrationsDto) throws Exception {

    }

    @Override
    public void removeAuctionRegistration(int arId) throws Exception {

    }

    @Override
    public Page<AuctionRegistrationsResponse> findAllAuctionRegistrations(PageRequest pageRequest) throws Exception {
        Page<AuctionRegistration> auctionRegistrations = auctionRegistrationsRepository.findAll(pageRequest);
        return auctionRegistrations.map(auctionRegistrationsConverter::toAuctionRegistrationsResponse);
    }

    @Override
    public Page<AuctionRegistrationsResponse> findAllAuctionRegistrationsByUserId(PageRequest pageRequest) throws Exception {
        String token = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
                .getRequest().getHeader("Authorization").substring(7);
        Integer userId = extractUserIdFromToken(token);
        Page<AuctionRegistration> auctionRegistrations = auctionRegistrationsRepository.findByUserId(userId, pageRequest);
        return auctionRegistrations.map(auctionRegistrationsConverter::toAuctionRegistrationsResponse);
    }

    @Override
    public AuctionRegistrationsResponse findAuctionRegistrationById(int arId) throws Exception {
        AuctionRegistration auctionRegistration = auctionRegistrationsRepository.findById(arId)
                .orElseThrow(() -> new RuntimeException("Auction not found"));
        AuctionRegistrationsResponse auctionRegistrationsResponse = auctionRegistrationsConverter.toAuctionRegistrationsResponse(auctionRegistration);
        return auctionRegistrationsResponse;
    }


    public Integer extractUserIdFromToken(String token) throws Exception {
        String userEmail = jwtService.extractUserEmail(token); // Extract email from token
        User user = userRepository.findByEmail(userEmail) // Find user by email
                .orElseThrow(() -> new Exception("User not found!!!"));
        return user.getId();
    }
}
