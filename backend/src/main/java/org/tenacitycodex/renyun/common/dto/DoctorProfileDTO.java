package org.tenacitycodex.renyun.common.dto;

import lombok.Builder;
import lombok.Data;
import org.tenacitycodex.renyun.module.user.entity.DoctorProfile;

@Data
@Builder
public class DoctorProfileDTO {
    private Long userId;
    private String hospital;
    private String department;
    private String title;
    private String speciality;
    private String bio;
    private String phone;

    public static DoctorProfileDTO from(DoctorProfile d) {
        if (d == null) return null;
        return DoctorProfileDTO.builder()
                .userId(d.getUserId())
                .hospital(d.getHospital())
                .department(d.getDepartment())
                .title(d.getTitle())
                .speciality(d.getSpeciality())
                .bio(d.getBio())
                .phone(d.getPhone())
                .build();
    }
}
