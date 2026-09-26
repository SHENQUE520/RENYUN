package org.tenacitycodex.renyun.common.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PatientRegisterRequest {
    @NotNull(message = "role不能为空")
    private String role;
    @NotNull(message = "username不能为空")
    private String username;
    @NotNull(message = "password不能为空")
    private String password;
    @NotNull(message = "name不能为空")
    private String name;
    private String gender;
    private Integer age;
    private String diagnosis;
    private String hospital;
    private Long doctorId;
    private String doctorFreeText;
    private String department;
    private String title;
    private String speciality;
}
