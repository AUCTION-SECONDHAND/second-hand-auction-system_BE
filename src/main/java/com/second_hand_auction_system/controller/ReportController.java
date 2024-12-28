package com.second_hand_auction_system.controller;

import com.second_hand_auction_system.dtos.request.report.ReplyReportDto;
import com.second_hand_auction_system.dtos.request.report.ReportDto;
import com.second_hand_auction_system.dtos.responses.ResponseListObject;
import com.second_hand_auction_system.dtos.responses.ResponseObject;
import com.second_hand_auction_system.dtos.responses.item.AuctionItemResponse;
import com.second_hand_auction_system.dtos.responses.report.ReportResponse;
import com.second_hand_auction_system.service.report.IReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PutMapping("/{reportId}")
    public ResponseEntity<?> updateReport(
            @PathVariable Integer reportId,
            @Valid @RequestBody ReplyReportDto reportDto
    ) throws Exception {
        reportService.updateReport(reportId, reportDto);
        return ResponseEntity.ok(
                ResponseObject.builder()
                        .status(HttpStatus.OK)
                        .message("Tạo báo cáo thành công")
                        .build()
        );
    }

    @GetMapping("/user")
    public ResponseEntity<?> getReportByUser(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) throws Exception {
        PageRequest pageRequest = PageRequest.of(page, limit);
        Page<ReportResponse> reportResponses = reportService.getReportsByUser(pageRequest);
        int totalPages = reportResponses.getTotalPages();
        Long totalOrder = reportResponses.getTotalElements();
        List<ReportResponse> reports = reportResponses.getContent();
        ResponseListObject<List<ReportResponse>> responseListObject = ResponseListObject.<List<ReportResponse>>builder()
                .data(reports)
                .totalElements(totalOrder)
                .totalPages(totalPages)
                .build();
        return ResponseEntity.ok(
                ResponseObject.builder()
                        .status(HttpStatus.OK)
                        .message("thành công")
                        .data(responseListObject)
                        .build()
        );
    }

    @GetMapping("")
    public ResponseEntity<?> getRepor(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) throws Exception {
        PageRequest pageRequest = PageRequest.of(page, limit);
        Page<ReportResponse> reportResponses = reportService.getReports(pageRequest);
        int totalPages = reportResponses.getTotalPages();
        Long totalOrder = reportResponses.getTotalElements();
        List<ReportResponse> reports = reportResponses.getContent();
        ResponseListObject<List<ReportResponse>> responseListObject = ResponseListObject.<List<ReportResponse>>builder()
                .data(reports)
                .totalElements(totalOrder)
                .totalPages(totalPages)
                .build();
        return ResponseEntity.ok(
                ResponseObject.builder()
                        .status(HttpStatus.OK)
                        .message("thành công")
                        .data(responseListObject)
                        .build()
        );
    }
}
