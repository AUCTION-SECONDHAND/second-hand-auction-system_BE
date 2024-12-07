package com.second_hand_auction_system.repositories;

import com.second_hand_auction_system.models.KnowYourCustomer;
import com.second_hand_auction_system.models.User;
import com.second_hand_auction_system.utils.KycStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface KnowYourCustomerRepository extends JpaRepository<KnowYourCustomer,Integer>{
    boolean existsByCccdNumber(String cccdNumber);
    Optional<KnowYourCustomer> findByUserId(int userId);

    Page<KnowYourCustomer> findByFullNameContainingIgnoreCase(String search, Pageable pageable);

    @Query("SELECT k.user FROM KnowYourCustomer k WHERE k.kycId = :kycId")
    Optional<User> findUserByKycId(@Param("kycId") Integer kycId);

    Optional<KnowYourCustomer> findByUserIdAndKycStatus(Integer id, KycStatus kycStatus);

    Optional<KnowYourCustomer> findByUserId(Integer userId);

    Optional<KnowYourCustomer> findByCccdNumber(@NotBlank(message = "CCCD number cannot be blank") @Size(min = 12, max = 12, message = "CCCD number must be exactly 12 characters long") @Pattern(regexp = "\\d{12}", message = "CCCD number must contain only digits") String cccdNumber);
}
