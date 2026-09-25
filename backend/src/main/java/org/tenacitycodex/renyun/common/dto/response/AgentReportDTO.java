package org.tenacitycodex.renyun.common.dto.response;

import lombok.Builder;
import lombok.Data;
import org.tenacitycodex.renyun.module.training.entity.AgentReport;

import java.time.LocalDateTime;

@Data
@Builder
public class AgentReportDTO {

    private Long id;
    private Long patientId;
    private Long trainingId;
    private String trainingAnalysis;
    private String riskAssessment;
    private String rehabTrend;
    private String doctorSummary;
    private String finalReport;
    private String suggestedTasks;
    private LocalDateTime createdAt;

    public static AgentReportDTO from(AgentReport report) {
        if (report == null) return null;
        return AgentReportDTO.builder()
                .id(report.getId())
                .patientId(report.getPatientId())
                .trainingId(report.getTrainingId())
                .trainingAnalysis(report.getTrainingAnalysis())
                .riskAssessment(report.getRiskAssessment())
                .rehabTrend(report.getRehabTrend())
                .doctorSummary(report.getDoctorSummary())
                .finalReport(report.getFinalReport())
                .suggestedTasks(report.getSuggestedTasks())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
