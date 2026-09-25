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
@Table(name = "users", indexes = {@Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_name", columnList = "username"),
        @Index(name = "idx_user_role", columnList = "role"),
        @Index(name = "idx_user_role_status", columnList = "role, status")})
public class User {
    @Id
    @Column(name = "user_id", updatable = false, nullable = false, unique = true)
    private Long id;

    @Column(name = "username", unique = true)
    private String username;

    @Column(name = "password", nullable = false)
    private String passwordHash;

    @Column(name = "email", unique = true)
    private String email;

    private int age;

    @Column(name = "role", nullable = false)
    @Builder.Default
    private String role = "patient";

    @Column(name = "name")
    private String name;

    @Column(name = "gender")
    private String gender;

    @Column(name = "status", nullable = false)
    @Builder.Default
    private String status = "pending";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
