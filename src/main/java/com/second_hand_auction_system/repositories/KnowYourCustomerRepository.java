package com.second_hand_auction_system.repositories;

import com.second_hand_auction_system.models.KnowYourCustomer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KnowYourCustomerRepository extends JpaRepository<KnowYourCustomer,Integer>{
    boolean existsByCccdNumber(String cccdNumber);
}
