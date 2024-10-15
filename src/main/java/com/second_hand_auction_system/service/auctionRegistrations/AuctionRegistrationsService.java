package com.second_hand_auction_system.service.auctionRegistrations;

import com.second_hand_auction_system.dtos.request.auctionRegistrations.AuctionRegistrationsDto;
import com.second_hand_auction_system.models.Auction;
import com.second_hand_auction_system.models.AuctionRegistration;
import com.second_hand_auction_system.models.User;
import com.second_hand_auction_system.models.WalletCustomer;
import com.second_hand_auction_system.repositories.AuctionRegistrationsRepository;
import com.second_hand_auction_system.repositories.AuctionRepository;
import com.second_hand_auction_system.repositories.UserRepository;
import com.second_hand_auction_system.repositories.WalletCustomerRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuctionRegistrationsService implements IAuctionRegistrationsService {
    private final AuctionRegistrationsRepository auctionRegistrationsRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final AuctionRepository auctionRepository;
    private final WalletCustomerRepository walletCustomerRepository;

    @Override
    public void addAuctionRegistration(AuctionRegistrationsDto auctionRegistrationsDto) throws Exception{
        WalletCustomer walletCustomerCheckBalance = walletCustomerRepository.findByUserIdAndBalanceGreaterThanEqual100(auctionRegistrationsDto.getUser());
        if(walletCustomerCheckBalance == null) {
            throw new Exception("You do not have enough money in your wallet, please top up");
        }

        User userExist = userRepository.findById(auctionRegistrationsDto.getUser())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Auction auctionExist = auctionRepository.findById(auctionRegistrationsDto.getAuction())
                .orElseThrow(() -> new RuntimeException("Auction not found"));
        AuctionRegistration auctionRegistration = modelMapper.map(auctionRegistrationsDto, AuctionRegistration.class);
        auctionRegistration.setUser(userExist);
        auctionRegistration.setAuction(auctionExist);
        auctionRegistrationsRepository.save(auctionRegistration);
    }

    @Override
    public void updateAuctionRegistration(int arId, AuctionRegistrationsDto auctionRegistrationsDto) throws Exception{

    }

    @Override
    public void removeAuctionRegistration(int arId) throws Exception{

    }
}
