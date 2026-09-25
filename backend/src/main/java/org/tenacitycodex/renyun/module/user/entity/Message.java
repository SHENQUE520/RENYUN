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
@Table(name = "messages", indexes = {
        @Index(name = "idx_messages_patient", columnList = "toPatientId"),
        @Index(name = "idx_messages_created", columnList = "createdAt")
})
public class Message {

    @Id
    @Column(name = "message_id", nullable = false, unique = true)
    private String id;

    @Column(name = "from_role", nullable = false)
    private String fromRole;

    @Column(name = "from_name", nullable = false)
    private String fromName;

    @Column(name = "to_patient_id", nullable = false)
    private String toPatientId;

    @Column(name = "type", nullable = false)
    @Builder.Default
    private String type = "text";

    @Column(name = "text", nullable = false, columnDefinition = "TEXT")
    @Builder.Default
    private String text = "";

    @Column(name = "time", nullable = false)
    private String time;

    @Column(name = "date", nullable = false)
    private String date;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean read = false;

    @Column(name = "recalled", nullable = false)
    @Builder.Default
    private Boolean recalled = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
