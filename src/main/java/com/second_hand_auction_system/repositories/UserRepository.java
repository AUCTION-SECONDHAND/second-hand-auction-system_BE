package com.second_hand_auction_system.repositories;

import com.second_hand_auction_system.models.User;
import com.second_hand_auction_system.utils.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmailAndStatusIsTrue(String email);

    boolean existsByEmail(@Email(message = "Invalid email format") @NotBlank(message = "Email is required") String email);

    Optional<User> findByEmail(String userEmail);


    @Query("SELECT COUNT(u) FROM User u WHERE MONTH(u.createAt) = :month AND YEAR(u.createAt) = :year")
    Long countUsersByPreviousMonth(@Param("month") int month, @Param("year") int year);

    @Query("SELECT COUNT(u) FROM User u WHERE MONTH(u.createAt) = :month AND YEAR(u.createAt) = :year")
    Long countUsersByMonthAndYear(@Param("month") int month, @Param("year") int year);


    Long countByCreateAtBetween(LocalDateTime start, LocalDateTime end);
}
