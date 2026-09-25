package org.tenacitycodex.renyun.common.dto.request;

import lombok.Data;

import java.util.Map;

@Data
public class LoginRequest {
    private String loginType;
    private Map<String, Object> credential;
}
