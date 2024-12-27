package com.second_hand_auction_system.service.report;

import com.second_hand_auction_system.dtos.request.report.ReportDto;

public interface IReportService {
    void createReport(ReportDto reportDto) throws Exception;
}
