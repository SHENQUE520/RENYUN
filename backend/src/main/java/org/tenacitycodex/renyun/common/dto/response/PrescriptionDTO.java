package org.tenacitycodex.renyun.common.dto.response;

import lombok.Builder;
import lombok.Data;
import org.tenacitycodex.renyun.module.training.entity.Prescription;

@Data
@Builder
public class PrescriptionDTO {

    private Long id;
    private Long patientId;
    private String exercise;
    private Double targetRom;
    private Integer targetDuration;
    private Integer frequency;
    private String notes;

    public static PrescriptionDTO from(Prescription prescription) {
        if (prescription == null) return null;
        return PrescriptionDTO.builder()
                .id(prescription.getId())
                .patientId(prescription.getPatientId())
                .exercise(prescription.getExercise())
                .targetRom(prescription.getTargetRom())
                .targetDuration(prescription.getTargetDuration())
                .frequency(prescription.getFrequency())
                .notes(prescription.getNotes())
                .build();
    }
}
