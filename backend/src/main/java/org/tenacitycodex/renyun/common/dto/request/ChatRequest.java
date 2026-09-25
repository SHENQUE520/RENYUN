package org.tenacitycodex.renyun.common.dto.request;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ChatRequest {
    private List<Map<String, Object>> messages;
    private Double temperature;
}
