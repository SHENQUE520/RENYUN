package org.tenacitycodex.renyun.common.dto;

import lombok.Builder;
import lombok.Data;
import org.tenacitycodex.renyun.module.user.entity.PatientProfile;

@Data
@Builder
public class PatientProfileDTO {
    private Long userId;
    private Integer age;
    private String phone;
    private String emergencyContact;
    private String surgeryDate;
    private String notes;
    private String diagnosis;
    private String hospital;
    private Long doctorId;
    private String doctorName;

    public static PatientProfileDTO from(PatientProfile p) {
        if (p == null) return null;
        return PatientProfileDTO.builder()
                .userId(p.getUserId())
                .age(p.getAge())
                .phone(p.getPhone())
                .emergencyContact(p.getEmergencyContact())
                .surgeryDate(p.getSurgeryDate())
                .notes(p.getNotes())
                .diagnosis(p.getDiagnosis())
                .hospital(p.getHospital())
                .doctorId(p.getDoctorId())
                .doctorName(p.getDoctorName())
                .build();
    }
}
