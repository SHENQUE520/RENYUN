package org.tenacitycodex.renyun.common.dto.request;

import lombok.Data;

@Data
public class TaskStatusUpdateRequest {
    private Boolean done;
    private String status;
}
