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
@Table(name = "risk_events", indexes = {
        @Index(name = "idx_risk_patient", columnList = "patientId"),
        @Index(name = "idx_risk_training", columnList = "trainingId")
})
public class RiskEvent {

    @Id
    @Column(name = "risk_event_id", updatable = false, nullable = false, unique = true)
    private Long id;

    @Column(name = "training_id", nullable = false)
    private Long trainingId;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "severity", nullable = false)
    private String severity;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
