package org.tenacitycodex.renyun.common.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TaskUpsertRequest {
    @NotNull(message = "name不能为空")
    private String name;
    private Integer count;
    private String unit;
    private String keyPoints;
    private String details;
}
