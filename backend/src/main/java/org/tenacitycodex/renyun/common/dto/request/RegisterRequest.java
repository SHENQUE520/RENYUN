package org.tenacitycodex.renyun.common.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotNull(message = "username不能为空")
    private String username;
    @NotNull(message = "name不能为空")
    private String name;
    @NotNull(message = "password不能为空")
    private String password;
    @NotNull(message = "gender不能为空")
    private String gender;
}
