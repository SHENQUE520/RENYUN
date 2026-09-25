package org.tenacitycodex.renyun.common.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 训练数据上传请求，对应 backend-api-spec.md §3.1 POST /api/report 请求体。
 * <p>
 * 在原 spec 基础上增加 patientId，用于多 Agent 链式分析时关联患者历史数据。
 */
@Data
public class TrainingUploadRequest {
    @NotNull
    private String mode;

    private String modeName;

    private Double durationSec;

    private Integer samples;

    @NotNull
    private TrainingStats stats;
}
