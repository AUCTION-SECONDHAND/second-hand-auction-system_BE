package com.second_hand_auction_system.repositories;

import com.second_hand_auction_system.models.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Integer> {
    Page<Report> findByUser_Id(int id, Pageable pageable);
}