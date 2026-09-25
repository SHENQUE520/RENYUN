package org.tenacitycodex.renyun.common.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 训练统计数据，对应 backend-api-spec.md §3.1 中的 stats 对象。
 */
@Data
public class TrainingStats {

    @NotNull
    private Double avgPitch;

    @NotNull
    private Double maxPitch;

    @NotNull
    private Double minPitch;

    @NotNull
    private Double avgRoll;

    @NotNull
    private Double maxRoll;

    @NotNull
    private Double minRoll;

    @NotNull
    private Double avgKed;

    @NotNull
    private Double standardRatio;

    @NotNull
    private Double adjustRatio;

    @NotNull
    private Double dangerRatio;
}
