package org.tenacitycodex.renyun.module.training.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "agent_reports", indexes = {
        @Index(name = "idx_agent_report_patient", columnList = "patientId"),
        @Index(name = "idx_agent_report_training", columnList = "trainingId")
})
public class AgentReport {

    @Id
    @Column(name = "agent_report_id", updatable = false, nullable = false, unique = true)
    private Long id;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "training_id", nullable = false)
    private Long trainingId;

    @Column(name = "training_analysis", columnDefinition = "TEXT")
    private String trainingAnalysis;

    @Column(name = "risk_assessment", columnDefinition = "TEXT")
    private String riskAssessment;

    @Column(name = "rehab_trend", columnDefinition = "TEXT")
    private String rehabTrend;

    @Column(name = "doctor_summary", columnDefinition = "TEXT")
    private String doctorSummary;

    @Column(name = "final_report", columnDefinition = "TEXT")
    private String finalReport;

    @Column(name = "suggested_tasks", columnDefinition = "TEXT")
    private String suggestedTasks;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
