package com.second_hand_auction_system.repositories;

import com.second_hand_auction_system.models.AuctionRegistration;
import com.second_hand_auction_system.utils.Registration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AuctionRegistrationsRepository extends JpaRepository<AuctionRegistration, Integer> {

//    Page<AuctionRegistration> findByUserId(@Param("userId") Integer userId, Pageable pageable);
    List<AuctionRegistration> findByUsersId(int id);
    AuctionRegistration findByUsersIdAndAuction_AuctionId(Integer user_id, Integer auction_auctionId);

    @Query("SELECT CASE WHEN COUNT(ar) > 0 THEN true ELSE false END " +
            "FROM AuctionRegistration ar JOIN ar.users u " +
            "WHERE u.id = :userId AND ar.registration = :registration")
    boolean existsAuctionRegistrationByUserIdAndRegistration(@Param("userId") Integer userId,
                                                             @Param("registration") Registration registration);}
