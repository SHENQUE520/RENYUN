package org.tenacitycodex.renyun.module.training.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.tenacitycodex.renyun.module.training.entity.Prescription;

import java.util.Optional;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    Optional<Prescription> findTopByPatientIdOrderByCreatedAtDesc(Long patientId);

    Optional<Prescription> findTopByPatientIdAndExerciseOrderByCreatedAtDesc(Long patientId, String exercise);
}
