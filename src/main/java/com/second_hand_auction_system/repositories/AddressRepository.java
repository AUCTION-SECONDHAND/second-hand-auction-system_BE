package com.second_hand_auction_system.repositories;

import com.second_hand_auction_system.models.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address , Integer> {
    List<Address> findByUserId(Integer userId);
    int countByUserId(Integer userId);
    Optional<Address> findByUserIdAndStatusIsTrue(Integer userId);
}
