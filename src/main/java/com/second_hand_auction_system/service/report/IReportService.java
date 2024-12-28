package com.second_hand_auction_system.service.report;

import com.second_hand_auction_system.dtos.request.report.ReplyReportDto;
import com.second_hand_auction_system.dtos.request.report.ReportDto;
import com.second_hand_auction_system.dtos.responses.report.ReportResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface IReportService {
    void createReport(ReportDto reportDto) throws Exception;

    void updateReport(int reportId, ReplyReportDto replyReportDto) throws Exception;

    Page<ReportResponse> getReportsByUser(PageRequest pageRequest) throws Exception;
    Page<ReportResponse> getReports(PageRequest pageRequest) throws Exception;
}
