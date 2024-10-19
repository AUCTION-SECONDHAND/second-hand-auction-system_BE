package com.second_hand_auction_system.repositories;

import com.second_hand_auction_system.models.KnowYourCustomer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KnowYourCustomerRepository extends JpaRepository<KnowYourCustomer,Integer>{
    boolean existsByCccdNumber(String cccdNumber);
    Optional<KnowYourCustomer> findByUserId(int userId);

    Page<KnowYourCustomer> findByFullNameContainingIgnoreCase(String search, Pageable pageable);

}
