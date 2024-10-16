package com.second_hand_auction_system.repositories;

import com.second_hand_auction_system.models.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<Address , Integer> {
}
