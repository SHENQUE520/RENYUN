package org.tenacitycodex.renyun.common.dto;

import lombok.Builder;
import lombok.Data;
import org.tenacitycodex.renyun.module.training.entity.RiskEvent;

import java.time.LocalDateTime;

@Data
@Builder
public class RiskEventDTO {

    private Long id;
    private Long trainingId;
    private Long patientId;
    private String eventType;
    private String severity;
    private String description;
    private LocalDateTime createdAt;

    public static RiskEventDTO from(RiskEvent event) {
        return RiskEventDTO.builder()
                .id(event.getId())
                .trainingId(event.getTrainingId())
                .patientId(event.getPatientId())
                .eventType(event.getEventType())
                .severity(event.getSeverity())
                .description(event.getDescription())
                .createdAt(event.getCreatedAt())
                .build();
    }
}
