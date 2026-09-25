package org.tenacitycodex.renyun.api.v1.training;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tenacitycodex.renyun.common.config.security.SecurityUtil;
import org.tenacitycodex.renyun.common.dto.ApiResponse;
import org.tenacitycodex.renyun.common.dto.response.AgentReportDTO;
import org.tenacitycodex.renyun.common.exceptions.ApiException;
import org.tenacitycodex.renyun.module.training.entity.AgentReport;
import org.tenacitycodex.renyun.module.training.service.AgentReportService;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {
    private final AgentReportService agentReportService;

    public ReportController(AgentReportService agentReportService) {
        this.agentReportService = agentReportService;
    }

    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<AgentReportDTO>> getLatestReport() {
        AgentReport report = agentReportService.getLatestByPatient(SecurityUtil.getCurrentUserId())
                .orElseThrow(() -> new ApiException(org.springframework.http.HttpStatus.NOT_FOUND, "暂无分析报告"));
        return ResponseEntity.ok(ApiResponse.ok(AgentReportDTO.from(report)));
    }
}
