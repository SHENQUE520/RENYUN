package org.tenacitycodex.renyun.api.v1.training;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tenacitycodex.renyun.common.annotation.RequireAuth;
import org.tenacitycodex.renyun.common.config.security.SecurityUtil;
import org.tenacitycodex.renyun.common.dto.ApiResponse;
import org.tenacitycodex.renyun.common.dto.TaskDTO;
import org.tenacitycodex.renyun.common.dto.TrainingHistoryDTO;
import org.tenacitycodex.renyun.common.dto.request.TrainingUploadRequest;
import org.tenacitycodex.renyun.common.dto.response.AgentReportDTO;
import org.tenacitycodex.renyun.common.dto.response.TrainingUploadResponse;
import org.tenacitycodex.renyun.common.exceptions.ApiException;
import org.tenacitycodex.renyun.module.agent.AgentChainService;
import org.tenacitycodex.renyun.module.training.entity.AgentReport;
import org.tenacitycodex.renyun.module.training.service.AgentReportService;
import org.tenacitycodex.renyun.module.user.entity.TrainingRecord;
import org.tenacitycodex.renyun.module.user.service.TaskService;
import org.tenacitycodex.renyun.module.user.service.TrainingService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/training")
@RequiredArgsConstructor
public class TrainingController {

    private final TrainingService trainingService;
    private final AgentChainService agentChainService;
    private final AgentReportService agentReportService;
    private final TaskService taskService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<TrainingUploadResponse>> uploadTraining(
            @Valid @RequestBody TrainingUploadRequest request) {

        Long userId = SecurityUtil.getCurrentUserId();
        log.info("[Training] 训练数据: patientId={}, mode={}, modeName={}",
                userId, request.getMode(), request.getModeName());

        TrainingRecord savedRecord = trainingService.uploadTraining(request, userId);
        try {
            agentChainService.runUploadChain(request, savedRecord, userId);
        } catch (Exception e) {
            log.error("[Training] Agent 链执行失败", e);
            throw new ApiException(org.springframework.http.HttpStatus.BAD_GATEWAY,
                    "训练数据已保存，但 AI 分析失败");
        }

        AgentReport report = agentReportService.getByTrainingId(savedRecord.getId()).orElse(null);
        List<TaskDTO> tasks = taskService.getTasksByPatientAndStatus(userId, "pending");

        TrainingUploadResponse response = TrainingUploadResponse.builder()
                .trainingId(savedRecord.getId())
                .report(AgentReportDTO.from(report))
                .suggestedTasks(tasks)
                .build();

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<TrainingHistoryDTO>>> getHistory(
            @RequestParam(defaultValue = "20") int limit) {
        List<TrainingHistoryDTO> history = trainingService.getRecentHistory(SecurityUtil.getCurrentUserId(), limit);
        return ResponseEntity.ok(ApiResponse.ok(history));
    }
}
