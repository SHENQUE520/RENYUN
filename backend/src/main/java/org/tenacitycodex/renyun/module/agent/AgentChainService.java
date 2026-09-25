package org.tenacitycodex.renyun.module.agent;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tenacitycodex.renyun.common.dto.AgentContext;
import org.tenacitycodex.renyun.common.dto.RomTrendDTO;
import org.tenacitycodex.renyun.common.dto.TrainingHistoryDTO;
import org.tenacitycodex.renyun.common.dto.request.TrainingUploadRequest;
import org.tenacitycodex.renyun.common.dto.response.PrescriptionDTO;
import org.tenacitycodex.renyun.module.training.entity.AgentReport;
import org.tenacitycodex.renyun.module.training.service.AgentReportService;
import org.tenacitycodex.renyun.module.training.service.KnowledgeService;
import org.tenacitycodex.renyun.module.training.service.PrescriptionService;
import org.tenacitycodex.renyun.module.user.entity.Task;
import org.tenacitycodex.renyun.module.user.entity.TrainingRecord;
import org.tenacitycodex.renyun.module.user.service.TaskService;
import org.tenacitycodex.renyun.module.user.service.TrainingService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentChainService {

    private final TrainingService trainingService;
    private final PrescriptionService prescriptionService;
    private final KnowledgeService knowledgeService;
    private final AgentReportService agentReportService;
    private final TaskService taskService;
    private final CoordinatorAgent coordinatorAgent;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final int HISTORY_LIMIT = 10;
    private static final int TREND_DAYS = 7;
    private static final Pattern JSON_ARRAY_PATTERN = Pattern.compile("\\[\\s*\\{.*?}\\s*]", Pattern.DOTALL);

    public void runUploadChain(TrainingUploadRequest request, TrainingRecord savedRecord, Long patientId) {

        List<TrainingHistoryDTO> recentHistory = trainingService.getRecentHistory(patientId, HISTORY_LIMIT);
        RomTrendDTO romTrend = trainingService.getRomTrend(patientId, TREND_DAYS);
        String exerciseName = request.getModeName() != null ? request.getModeName() : request.getMode();
        PrescriptionDTO prescription = prescriptionService
                .getLatestPrescription(patientId, exerciseName)
                .orElse(prescriptionService.getLatestPrescription(patientId).orElse(null));
        String knowledgeSnippet = knowledgeService.getKnowledgeSnippet("动作规范", exerciseName);

        AgentContext context = AgentContext.builder()
                .patientId(patientId)
                .patientName(extractPatientName(patientId))
                .currentTraining(savedRecord)
                .recentHistory(recentHistory)
                .romTrend(romTrend)
                .prescription(prescription)
                .knowledgeSnippet(knowledgeSnippet)
                .build();

        context = coordinatorAgent.executeTrainingChain(context);

        AgentReport report = AgentReport.builder()
                .patientId(patientId)
                .trainingId(savedRecord.getId())
                .trainingAnalysis(context.getTrainingAnalysisResult())
                .riskAssessment(context.getRiskAssessmentResult())
                .rehabTrend(context.getRehabTrendResult())
                .doctorSummary(context.getDoctorSummaryResult())
                .finalReport(buildFinalReport(context))
                .suggestedTasks(extractTasksJson(context.getDoctorSummaryResult()))
                .build();
        agentReportService.save(report);

        List<Task> createdTasks = createSuggestedTasks(patientId, context.getDoctorSummaryResult());
        if (!createdTasks.isEmpty()) {
            taskService.saveAll(createdTasks);
        }

    }

    private String buildFinalReport(AgentContext context) {
        return String.format("【训练分析】%n%s%n%n【风险评估】%n%s%n%n【康复趋势】%n%s%n%n【医生建议】%n%s",
                safe(context.getTrainingAnalysisResult()),
                safe(context.getRiskAssessmentResult()),
                safe(context.getRehabTrendResult()),
                safe(context.getDoctorSummaryResult()));
    }

    private List<Task> createSuggestedTasks(Long patientId, String doctorSummary) {
        List<Task> tasks = new ArrayList<>();
        if (doctorSummary == null) return tasks;

        Matcher matcher = JSON_ARRAY_PATTERN.matcher(doctorSummary);
        if (!matcher.find()) {
            log.warn("[AgentChain] 未在医生摘要中解析到任务 JSON");
            return tasks;
        }
        String json = matcher.group();
        try {
            List<Map<String, Object>> rawTasks = objectMapper.readValue(json, new TypeReference<>() {});
            for (Map<String, Object> raw : rawTasks) {
                String name = String.valueOf(raw.getOrDefault("name", "康复训练"));
                Object countObj = raw.get("count");
                Integer count = countObj instanceof Number ? ((Number) countObj).intValue() : null;
                String unit = String.valueOf(raw.getOrDefault("unit", "次"));
                String description = String.valueOf(raw.getOrDefault("description", ""));
                tasks.add(Task.builder()
                        .patientId(patientId)
                        .name(name)
                        .count(count)
                        .unit(unit)
                        .description(description)
                        .status("pending")
                        .source("agent")
                        .build());
            }
        } catch (Exception e) {
            log.warn("[AgentChain] 解析任务 JSON 失败: {}", json, e);
        }
        return tasks;
    }

    private String extractTasksJson(String doctorSummary) {
        if (doctorSummary == null) return null;
        Matcher matcher = JSON_ARRAY_PATTERN.matcher(doctorSummary);
        return matcher.find() ? matcher.group() : null;
    }

    private String extractPatientName(Long patientId) {
        return "患者" + patientId;
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
