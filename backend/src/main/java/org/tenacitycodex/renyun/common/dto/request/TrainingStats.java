package org.tenacitycodex.renyun.common.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 训练统计数据，字段对齐 web/server.js 中 /api/report 请求的 stats 结构。
 */
@Data
public class TrainingStats {

    // —— Pitch（大腿俯仰角）——
    @NotNull(message = "avgPitch不能为空")
    private Double avgPitch;
    @NotNull(message = "maxPitch不能为空")
    private Double maxPitch;
    @NotNull(message = "minPitch不能为空")
    private Double minPitch;

    // —— Roll（大腿翻滚角）——
    @NotNull(message = "avgRoll不能为空")
    private Double avgRoll;
    @NotNull(message = "maxRoll不能为空")
    private Double maxRoll;
    @NotNull(message = "minRoll不能为空")
    private Double minRoll;

    // —— KED（膝关节夹角）——
    @NotNull(message = "avgKed不能为空")
    private Double avgKed;

    // —— 姿态占比 ——
    @NotNull(message = "standardRatio不能为空")
    private Double standardRatio;
    @NotNull(message = "adjustRatio不能为空")
    private Double adjustRatio;
    @NotNull(message = "dangerRatio不能为空")
    private Double dangerRatio;
}
