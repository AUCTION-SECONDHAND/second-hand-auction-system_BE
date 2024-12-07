package com.second_hand_auction_system.repositories;

import com.second_hand_auction_system.models.AuctionRegistration;
import com.second_hand_auction_system.models.User;
import com.second_hand_auction_system.utils.Registration;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AuctionRegistrationsRepository extends JpaRepository<AuctionRegistration, Integer> {

    @Query("SELECT ar FROM AuctionRegistration ar JOIN ar.users u WHERE u.id = :userId")
    Page<AuctionRegistration> findAuctionRegistrationsByUserId(@Param("userId") Integer userId, Pageable pageable);    List<AuctionRegistration> findByUsersId(int id);
    AuctionRegistration findByUsersIdAndAuction_AuctionId(Integer user_id, Integer auction_auctionId);
    //AuctionRegistration findByUsersIdAndAuction_AuctionId
    List<AuctionRegistration> findByAuction_AuctionId(Integer auction_auctionId);
    @Query("SELECT CASE WHEN COUNT(ar) > 0 THEN true ELSE false END " +
            "FROM AuctionRegistration ar JOIN ar.users u " +
            "WHERE u.id = :userId AND ar.auction.auctionId = :auctionId AND ar.registration = true")
    boolean existsAuctionRegistrationByUserIdAndAuctionIdAndRegistrationTrue(
            @Param("userId") Integer userId,
            @Param("auctionId") Integer auctionId);



//    AuctionRegistration findByAuction_AuctionIdAndUsersContaining(@NotNull(message = "Auction ID is required") Integer auction, User requester);


    @Query("SELECT COUNT(ur) > 0 FROM AuctionRegistration ar JOIN ar.users ur WHERE ar.auctionRegistrationId = :auctionRegistrationId AND ur.id = :userId")
    boolean existsByUserIdAndAuctionRegistrationId(@Param("userId") Integer userId, @Param("auctionRegistrationId") Integer auctionRegistrationId);

    @Query("SELECT ar.auctionRegistrationId FROM AuctionRegistration ar WHERE ar.auction.auctionId = :auctionId")
    Integer findAuctionRegistrationIdByAuctionId(@Param("auctionId") Integer auctionId);

    Optional<AuctionRegistration> findByAuction_AuctionIdAndUsers_Id(@NotNull(message = "Auction ID is required") Integer auction, Integer id);

    @Query("SELECT COUNT(ar) FROM AuctionRegistration ar WHERE ar.auction.auctionId = :auctionId AND ar.registration = true")
    Integer countRegistrationsByAuctionId(@Param("auctionId") Integer auctionId);
}
