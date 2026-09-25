package org.tenacitycodex.renyun.common.dto.request;

import lombok.Data;

@Data
public class TaskUpsertRequest {
    private String name;
    private Integer count;
    private String unit;
    private String keyPoints;
    private String details;
}
