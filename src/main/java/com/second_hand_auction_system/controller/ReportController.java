package com.second_hand_auction_system.controller;

import com.second_hand_auction_system.dtos.request.report.ReportDto;
import com.second_hand_auction_system.dtos.responses.ResponseObject;
import com.second_hand_auction_system.service.report.IReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/report")
public class ReportController {
    private final IReportService reportService;

    @PostMapping
    public ResponseEntity<?> createReport(
            @Valid @RequestBody ReportDto reportDto
    ) throws Exception {
        reportService.createReport(reportDto);
        return ResponseEntity.ok(
                ResponseObject.builder()
                        .status(HttpStatus.OK)
                        .message("Tạo báo cáo thành công")
                        .build()
        );
    }
}
