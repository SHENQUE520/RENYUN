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
@Table(name = "rehab_knowledge", indexes = {
        @Index(name = "idx_knowledge_category", columnList = "category")
})
public class RehabKnowledge {

    @Id
    @Column(name = "knowledge_id", updatable = false, nullable = false, unique = true)
    private Long id;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "keywords")
    private String keywords;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
