package org.tenacitycodex.renyun.common.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class PatientReportRequest {
    @NotNull(message = "patientName不能为空")
    private String patientName;
    @NotNull(message = "gender不能为空")
    private String gender;
    @NotNull(message = "age不能为空")
    private Integer age;
    private String surgeryDate;
    private String doctor;
    @NotNull(message = "status不能为空")
    private String status;
    private List<Map<String, Object>> records;
    private List<Map<String, Object>> tasks;
    private List<Map<String, Object>> history;
    private Integer streakDays;
    private Integer totalCheckins;
}
