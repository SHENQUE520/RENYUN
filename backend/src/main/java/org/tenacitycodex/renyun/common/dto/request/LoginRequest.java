package org.tenacitycodex.renyun.common.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class LoginRequest {
    @NotNull(message = "loginType不能为空")
    private String loginType;
    private Map<String, Object> credential;
}
