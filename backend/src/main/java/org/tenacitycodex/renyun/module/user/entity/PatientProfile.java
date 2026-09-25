package org.tenacitycodex.renyun.module.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "patient_profiles")
public class PatientProfile {

    @Id
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    private Integer age;

    private String phone;

    @Column(name = "emergency_contact")
    private String emergencyContact;

    @Column(name = "surgery_date")
    private String surgeryDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    private String diagnosis;

    private String hospital;

    @Column(name = "doctor_id")
    private Long doctorId;

    @Column(name = "doctor_name")
    private String doctorName;
}
