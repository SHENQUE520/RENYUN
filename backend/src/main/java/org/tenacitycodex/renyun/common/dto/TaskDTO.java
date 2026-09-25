package org.tenacitycodex.renyun.common.dto;

import lombok.Builder;
import lombok.Data;
import org.tenacitycodex.renyun.module.user.entity.Task;

import java.time.LocalDateTime;

@Data
@Builder
public class TaskDTO {

    private Long id;
    private Long patientId;
    private String name;
    private Integer count;
    private String unit;
    private String description;
    private String status;
    private String source;
    private LocalDateTime createdAt;

    public static TaskDTO from(Task task) {
        return TaskDTO.builder()
                .id(task.getId())
                .patientId(task.getPatientId())
                .name(task.getName())
                .count(task.getCount())
                .unit(task.getUnit())
                .description(task.getDescription())
                .status(task.getStatus())
                .source(task.getSource())
                .createdAt(task.getCreatedAt())
                .build();
    }

    public Task toEntity() {
        return Task.builder()
                .id(this.id)
                .patientId(this.patientId)
                .name(this.name)
                .count(this.count)
                .unit(this.unit)
                .description(this.description)
                .status(this.status)
                .source(this.source)
                .build();
    }
}
