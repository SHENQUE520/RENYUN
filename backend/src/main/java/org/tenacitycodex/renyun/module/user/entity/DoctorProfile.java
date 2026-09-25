package org.tenacitycodex.renyun.module.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "doctor_profiles")
public class DoctorProfile {

    @Id
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    private String hospital;

    private String department;

    private String title;

    private String speciality;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    private String phone;
}
