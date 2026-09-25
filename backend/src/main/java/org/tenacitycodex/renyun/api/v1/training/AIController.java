package org.tenacitycodex.renyun.api.v1.training;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tenacitycodex.renyun.common.dto.ApiResponse;
import org.tenacitycodex.renyun.common.dto.request.ChatRequest;
import org.tenacitycodex.renyun.common.dto.request.PatientReportRequest;
import org.tenacitycodex.renyun.common.dto.request.ReportRequest;
import org.tenacitycodex.renyun.module.agent.AiService;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AIController {

    private final AiService aiService;

    @PostMapping("/report")
    public ResponseEntity<ApiResponse<String>> generateReport(@RequestBody ReportRequest request) {
        String report = aiService.generateReport(
                request.getMode(),
                request.getModeName(),
                request.getDurationSec(),
                request.getSamples(),
                request.getStats());
        return ResponseEntity.ok(ApiResponse.ok(report));
    }

    @PostMapping("/patient-report")
    public ResponseEntity<ApiResponse<String>> generatePatientReport(@RequestBody PatientReportRequest request) {
        Map<String, Object> data = new HashMap<>();
        data.put("patientName", request.getPatientName());
        data.put("gender", request.getGender());
        data.put("age", request.getAge());
        data.put("surgeryDate", request.getSurgeryDate());
        data.put("doctor", request.getDoctor());
        data.put("status", request.getStatus());
        data.put("records", request.getRecords());
        data.put("tasks", request.getTasks());
        data.put("history", request.getHistory());
        data.put("streakDays", request.getStreakDays());
        data.put("totalCheckins", request.getTotalCheckins());
        String report = aiService.generatePatientReport(data);
        return ResponseEntity.ok(ApiResponse.ok(report));
    }

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<String>> chat(@RequestBody ChatRequest request) {
        String reply = aiService.chat(request.getMessages(), request.getTemperature());
        return ResponseEntity.ok(ApiResponse.ok(reply));
    }
}
