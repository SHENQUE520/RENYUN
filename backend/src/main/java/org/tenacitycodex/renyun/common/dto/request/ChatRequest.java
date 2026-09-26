package org.tenacitycodex.renyun.common.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ChatRequest {
    @NotNull(message = "messages不能为空")
    private List<Map<String, Object>> messages;
    private Double temperature;
}
