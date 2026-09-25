package org.tenacitycodex.renyun.common.dto.request;

import lombok.Data;

@Data
public class ReportRequest {
    private String mode;
    private String modeName;
    private Double durationSec;
    private Integer samples;
    private TrainingStats stats;
}
