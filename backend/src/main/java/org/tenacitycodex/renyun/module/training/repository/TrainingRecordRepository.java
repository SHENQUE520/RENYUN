package org.tenacitycodex.renyun.module.training.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.tenacitycodex.renyun.module.user.entity.TrainingRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingRecordRepository extends JpaRepository<TrainingRecord, Long> {

    List<TrainingRecord> findByPatientIdOrderByTrainingDateDesc(Long patientId);

    List<TrainingRecord> findByPatientIdAndTrainingDateBetweenOrderByTrainingDateAsc(
            Long patientId, LocalDate start, LocalDate end);

    Optional<TrainingRecord> findTopByPatientIdOrderByTrainingDateDesc(Long patientId);

    List<TrainingRecord> findByPatientIdOrderByTrainingDateDesc(Long patientId, Pageable pageable);
}
