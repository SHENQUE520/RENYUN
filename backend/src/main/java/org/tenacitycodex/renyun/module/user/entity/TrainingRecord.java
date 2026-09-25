package org.tenacitycodex.renyun.module.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 训练记录实体，字段对齐 backend-api-spec.md §3.1 的请求数据结构。
 */
@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "training_records", indexes = {
        @Index(name = "idx_training_patient", columnList = "patient_id"),
        @Index(name = "idx_training_patient_date", columnList = "patient_id, training_date")
})
public class TrainingRecord {

    @Id
    @Column(name = "training_id", updatable = false, nullable = false, unique = true)
    private Long id;

    @Column(name = "patient_id")
    private Long patientId;

    @Column(name = "mode", nullable = false)
    private String mode;

    @Column(name = "mode_name")
    private String modeName;

    @Column(name = "training_date", nullable = false)
    private LocalDate trainingDate;

    @Column(name = "duration_sec")
    private Double durationSec;

    @Column(name = "samples")
    private Integer samples;

    // —— Pitch（大腿俯仰角）——
    @Column(name = "avg_pitch")
    private Double avgPitch;

    @Column(name = "max_pitch")
    private Double maxPitch;

    @Column(name = "min_pitch")
    private Double minPitch;

    // —— Roll（大腿翻滚角）——
    @Column(name = "avg_roll")
    private Double avgRoll;

    @Column(name = "max_roll")
    private Double maxRoll;

    @Column(name = "min_roll")
    private Double minRoll;

    // —— KED（膝关节夹角）——
    @Column(name = "avg_ked")
    private Double avgKed;

    // —— 姿态占比 ——
    @Column(name = "standard_ratio")
    private Double standardRatio;

    @Column(name = "adjust_ratio")
    private Double adjustRatio;

    @Column(name = "danger_ratio")
    private Double dangerRatio;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
