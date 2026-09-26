package org.tenacitycodex.renyun.common.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportRequest {
    @NotNull(message = "mode不能为空")
    private String mode;
    private String modeName;
    private Double durationSec;
    private Integer samples;
    @NotNull(message = "stats不能为空")
    private TrainingStats stats;
}
