package org.tenacitycodex.renyun.common.dto;

import lombok.Builder;
import lombok.Data;
import org.tenacitycodex.renyun.common.dto.response.PrescriptionDTO;
import org.tenacitycodex.renyun.module.user.entity.TrainingRecord;

import java.util.List;

@Data
@Builder
public class AgentContext {

    private Long patientId;
    private String patientName;
    private TrainingRecord currentTraining;
    private List<TrainingHistoryDTO> recentHistory;
    private RomTrendDTO romTrend;
    private PrescriptionDTO prescription;
    private String knowledgeSnippet;

    private String trainingAnalysisResult;
    private String riskAssessmentResult;
    private String rehabTrendResult;
    private String doctorSummaryResult;
}
