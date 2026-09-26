package org.tenacitycodex.renyun.api.v1.training;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tenacitycodex.renyun.common.annotation.RequireAuth;
import org.tenacitycodex.renyun.common.dto.ApiResponse;
import org.tenacitycodex.renyun.common.dto.TaskDTO;
import org.tenacitycodex.renyun.common.dto.request.TaskStatusUpdateRequest;
import org.tenacitycodex.renyun.common.dto.request.TaskUpsertRequest;
import org.tenacitycodex.renyun.common.service.SseEventService;
import org.tenacitycodex.renyun.module.user.service.TaskService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final SseEventService sseEventService;
    @RequireAuth
    @GetMapping("/{patientId}")
    public ResponseEntity<ApiResponse<List<TaskDTO>>> getTasksByPatient(@PathVariable Long patientId) {
        List<TaskDTO> tasks = taskService.getTasksByPatient(patientId);
        return ResponseEntity.ok(ApiResponse.ok(tasks));
    }
    @RequireAuth
    @PostMapping("/{patientId}")
    public ResponseEntity<ApiResponse<TaskDTO>> upsertTask(
            @PathVariable Long patientId,
            @RequestBody @Valid TaskUpsertRequest request) {
        TaskDTO task = taskService.upsertTaskByName(
                patientId,
                request.getName(),
                request.getCount(),
                request.getUnit(),
                request.getKeyPoints(),
                request.getDetails());
        sseEventService.broadcast("{\"type\":\"new_task\",\"patientId\":\"" + patientId + "\"}");
        return ResponseEntity.ok(ApiResponse.ok(task));
    }
    @RequireAuth
    @PatchMapping("/{taskId}")
    public ResponseEntity<ApiResponse<TaskDTO>> updateTaskStatus(
            @PathVariable Long taskId,
            @RequestBody @Valid TaskStatusUpdateRequest request) {
        TaskDTO task = taskService.updateTaskStatus(taskId, request.getDone(), request.getStatus());
        boolean done = "done".equals(task.getStatus());
        sseEventService.broadcast("{\"type\":\"task_update\",\"taskId\":\"" + taskId
                + "\",\"patientId\":\"" + task.getPatientId() + "\",\"done\":" + done + "}");
        return ResponseEntity.ok(ApiResponse.ok(task));
    }
}
