package org.tenacitycodex.renyun.module.user.entity;

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
@Table(name = "tasks", indexes = {
        @Index(name = "idx_task_patient", columnList = "patientId"),
        @Index(name = "idx_task_status", columnList = "patientId, status")
})
public class Task {

    @Id
    @Column(name = "task_id", updatable = false, nullable = false, unique = true)
    private Long id;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "count")
    private Integer count;

    @Column(name = "unit")
    private String unit;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "source", nullable = false)
    private String source;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
