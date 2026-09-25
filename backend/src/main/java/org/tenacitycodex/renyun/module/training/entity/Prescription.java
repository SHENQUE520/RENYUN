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
@Table(name = "prescriptions", indexes = {
        @Index(name = "idx_prescription_patient", columnList = "patientId")
})
public class Prescription {

    @Id
    @Column(name = "prescription_id", updatable = false, nullable = false, unique = true)
    private Long id;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "exercise")
    private String exercise;

    @Column(name = "target_rom")
    private Double targetRom;

    @Column(name = "target_duration")
    private Integer targetDuration;

    @Column(name = "frequency")
    private Integer frequency;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
