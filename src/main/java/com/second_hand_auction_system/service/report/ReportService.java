package com.second_hand_auction_system.service.report;


import com.second_hand_auction_system.dtos.request.report.ReplyReportDto;
import com.second_hand_auction_system.dtos.request.report.ReportDto;
import com.second_hand_auction_system.dtos.responses.report.ReportResponse;
import com.second_hand_auction_system.models.Order;
import com.second_hand_auction_system.models.Report;
import com.second_hand_auction_system.models.User;
import com.second_hand_auction_system.repositories.OrderRepository;
import com.second_hand_auction_system.repositories.ReportRepository;
import com.second_hand_auction_system.repositories.UserRepository;
import com.second_hand_auction_system.service.jwt.IJwtService;
import com.second_hand_auction_system.utils.ReportStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ReportService implements IReportService {
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final IJwtService jwtService;

    @Override
    public void createReport(ReportDto reportDto) throws Exception {
        String authHeader = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
                .getRequest().getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new Exception("Unauthorized");
        }
        Order orderExisted = orderRepository.findById(reportDto.getOrderId())
                .orElseThrow(() -> new Exception("Đơn hàng không tìm thấy"));

        String token = authHeader.substring(7);
        String userEmail = jwtService.extractUserEmail(token);
        var requester = userRepository.findByEmailAndStatusIsTrue(userEmail).orElse(null);
        if (requester == null) {
            throw new Exception("Không tìm thấy người dùng");
        }
        Report report = Report.builder()
                .evidence(reportDto.getEvidence())
                .reason(reportDto.getReason())
                .type(reportDto.getType())
                .order(orderExisted)
                .user(requester)
                .build();
        report.setStatus(ReportStatus.PENDING);
        report.setCreateBy(requester.getFullName());
        report.setProcessedBy(requester.getFullName());
        reportRepository.save(report);
    }

    @Override
    public void updateReport(int reportId, ReplyReportDto replyReportDto) throws Exception {
        String authHeader = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
                .getRequest().getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new Exception("Unauthorized");
        }

        String token = authHeader.substring(7);
        String userEmail = jwtService.extractUserEmail(token);
        var requester = userRepository.findByEmailAndStatusIsTrue(userEmail).orElse(null);
        if (requester == null) {
            throw new Exception("Không tìm thấy người dùng");
        }
//        Order orderExisted = orderRepository.findById(replyReportDto.getOrderId())
//                .orElseThrow(() -> new Exception("Đơn hàng không tìm thấy"));
        Report reportExisted = reportRepository.findById(reportId)
                .orElseThrow(() -> new Exception("Không tìm thấy báo cáo"));
        reportExisted.setProcessedBy(requester.getFullName());
        reportExisted.setStatus(replyReportDto.getStatus());
        reportExisted.setResponseCreateTime(LocalDateTime.now());
        reportExisted.setResponseUpdateTime(LocalDateTime.now());
        reportExisted.setResponseMessage(replyReportDto.getResponseMessage());
        reportExisted.setTicketId(replyReportDto.getTicketId());
        //reportExisted.setEvidence(replyReportDto.getEvidence());
        //reportExisted.setOrder(orderExisted);
        reportRepository.save(reportExisted);
    }

    @Override
    public Page<ReportResponse> getReportsByUser(PageRequest pageRequest) throws Exception {
        String authHeader = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
                .getRequest().getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new Exception("Unauthorized");
        }

        String token = authHeader.substring(7);
        String userEmail = jwtService.extractUserEmail(token);
        var requester = userRepository.findByEmailAndStatusIsTrue(userEmail).orElse(null);
        if (requester == null) {
            throw new Exception("Không tìm thấy người dùng");
        }
        Page<Report> reports = reportRepository.findByUser_Id(requester.getId(), pageRequest);
        return reports.map(report -> ReportResponse.builder()
                .reportId(report.getReportId())
                .reason(report.getReason())
                .evidence(report.getEvidence())
                .type(report.getType())
                .createBy(report.getCreateBy())
                .priority(report.getPriority())
                .status(report.getStatus())
                .processedBy(report.getProcessedBy())
                .responseMessage(report.getResponseMessage())
                .responseCreateTime(report.getResponseCreateTime())
                .responseUpdateTime(report.getResponseUpdateTime())
                .createdAt(report.getCreateAt())
                .updatedAt(report.getUpdateAt())
                .orderId(report.getOrder() != null ? report.getOrder().getOrderId() : null)
                .orderCode(report.getOrder() != null ? report.getOrder().getOrderCode() : null)
                .ticketId(report.getTicketId())
                .build());
    }

    @Override
    public Page<ReportResponse> getReports(PageRequest pageRequest) throws Exception {
        Page<Report> reports = reportRepository.findAll(pageRequest);
        return reports.map(report -> ReportResponse.builder()
                .reportId(report.getReportId())
                .reason(report.getReason())
                .evidence(report.getEvidence())
                .type(report.getType())
                .createBy(report.getCreateBy())
                .priority(report.getPriority())
                .status(report.getStatus())
                .processedBy(report.getProcessedBy())
                .responseMessage(report.getResponseMessage())
                .responseCreateTime(report.getResponseCreateTime())
                .responseUpdateTime(report.getResponseUpdateTime())
                .orderId(report.getOrder().getOrderId())
                .orderCode(report.getOrder() != null ? report.getOrder().getOrderCode() : null)
                .ticketId(report.getTicketId())
                .build());
    }

    public Integer extractUserIdFromToken(String token) throws Exception {
        String userEmail = jwtService.extractUserEmail(token); // Extract email from token
        User user = userRepository.findByEmail(userEmail) // Find user by email
                .orElseThrow(() -> new Exception("User not found!!!"));
        return user.getId();
    }
}
