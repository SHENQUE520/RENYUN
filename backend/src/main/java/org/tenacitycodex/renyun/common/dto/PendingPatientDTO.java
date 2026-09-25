package org.tenacitycodex.renyun.common.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PendingPatientDTO {
    private Long id;
    private String name;
    private String gender;
    private String status;
    private Integer age;
    private String diagnosis;
    private String hospital;
    private String doctorName;
    private Long doctorId;
    private LocalDateTime createdAt;
}
