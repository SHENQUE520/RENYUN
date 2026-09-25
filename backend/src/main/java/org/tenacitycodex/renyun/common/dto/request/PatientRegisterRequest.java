package org.tenacitycodex.renyun.common.dto.request;

import lombok.Data;

@Data
public class PatientRegisterRequest {
    private String role;
    private String username;
    private String password;
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
