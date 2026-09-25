package org.tenacitycodex.renyun.common.dto.request;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class PatientReportRequest {
    private String patientName;
    private String gender;
    private Integer age;
    private String surgeryDate;
    private String doctor;
    private String status;
    private List<Map<String, Object>> records;
    private List<Map<String, Object>> tasks;
    private List<Map<String, Object>> history;
    private Integer streakDays;
    private Integer totalCheckins;
}
