package com.second_hand_auction_system.service.report;


import com.second_hand_auction_system.dtos.request.report.ReportDto;
import com.second_hand_auction_system.models.Report;
import com.second_hand_auction_system.models.User;
import com.second_hand_auction_system.repositories.ReportRepository;
import com.second_hand_auction_system.repositories.UserRepository;
import com.second_hand_auction_system.service.jwt.IJwtService;
import com.second_hand_auction_system.utils.ReportStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ReportService implements IReportService {
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final IJwtService jwtService;

    @Override
    public void createReport(ReportDto reportDto) throws Exception {
        String authHeader = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
                .getRequest().getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new Exception("Unauthorized");
        }

        String token = authHeader.substring(7);
        String userEmail = jwtService.extractUserEmail(token);
        var requester = userRepository.findByEmailAndStatusIsTrue(userEmail).orElse(null);
        if (requester == null) {
            throw new Exception("User not found");
        }
        Report report = Report.builder()
                .evidence(reportDto.getEvidence())
                .reason(reportDto.getReason())
                .type(reportDto.getType())
                .user(requester)
                .build();
        report.setStatus(ReportStatus.PENDING);
        report.setCreateBy(requester.getFullName());
        report.setProcessedBy(requester.getFullName());
        reportRepository.save(report);
    }

    public Integer extractUserIdFromToken(String token) throws Exception {
        String userEmail = jwtService.extractUserEmail(token); // Extract email from token
        User user = userRepository.findByEmail(userEmail) // Find user by email
                .orElseThrow(() -> new Exception("User not found!!!"));
        return user.getId();
    }
}
