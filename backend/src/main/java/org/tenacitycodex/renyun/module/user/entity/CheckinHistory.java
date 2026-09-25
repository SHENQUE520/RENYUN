package org.tenacitycodex.renyun.module.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "checkin_history", indexes = {
        @Index(name = "idx_checkin_patient", columnList = "patientId")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_checkin_patient_date", columnNames = {"patientId", "date"})
})
public class CheckinHistory {

    @Id
    @Column(name = "checkin_id", nullable = false, unique = true)
    private String id;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "date", nullable = false)
    private String date;

    @Column(name = "done", nullable = false)
    @Builder.Default
    private Boolean done = true;
}
