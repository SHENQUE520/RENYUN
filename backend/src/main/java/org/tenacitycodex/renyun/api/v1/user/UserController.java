package org.tenacitycodex.renyun.api.v1.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tenacitycodex.renyun.common.config.security.SecurityUtil;
import org.tenacitycodex.renyun.common.dto.ApiResponse;
import org.tenacitycodex.renyun.common.dto.RomTrendDTO;
import org.tenacitycodex.renyun.common.dto.TaskDTO;
import org.tenacitycodex.renyun.module.user.service.TaskService;
import org.tenacitycodex.renyun.module.user.service.TrainingService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final TrainingService trainingService;
    private final TaskService taskService;

    public UserController(TrainingService trainingService, TaskService taskService) {
        this.trainingService = trainingService;
        this.taskService = taskService;
    }

    @GetMapping("/trend/rom")
    public ResponseEntity<ApiResponse<RomTrendDTO>> getRomTrend(
            @RequestParam(defaultValue = "7") int days) {
        RomTrendDTO trend = trainingService.getRomTrend(SecurityUtil.getCurrentUserId(), days);
        return ResponseEntity.ok(ApiResponse.ok(trend));
    }

    @GetMapping("/tasks")
    public ResponseEntity<ApiResponse<List<TaskDTO>>> getTasks() {
        List<TaskDTO> tasks = taskService.getTasksByPatient(SecurityUtil.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.ok(tasks));
    }
}
