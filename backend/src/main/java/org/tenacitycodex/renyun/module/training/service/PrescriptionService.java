package org.tenacitycodex.renyun.module.training.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tenacitycodex.renyun.common.dto.response.PrescriptionDTO;
import org.tenacitycodex.renyun.module.training.entity.Prescription;
import org.tenacitycodex.renyun.module.training.repository.PrescriptionRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;

    public Optional<PrescriptionDTO> getLatestPrescription(Long patientId) {
        return prescriptionRepository.findTopByPatientIdOrderByCreatedAtDesc(patientId)
                .map(PrescriptionDTO::from);
    }

    public Optional<PrescriptionDTO> getLatestPrescription(Long patientId, String exercise) {
        return prescriptionRepository.findTopByPatientIdAndExerciseOrderByCreatedAtDesc(patientId, exercise)
                .map(PrescriptionDTO::from);
    }

    public Prescription save(Prescription prescription) {
        return prescriptionRepository.save(prescription);
    }
}
