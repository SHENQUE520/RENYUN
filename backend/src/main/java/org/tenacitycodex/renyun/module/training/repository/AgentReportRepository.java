package org.tenacitycodex.renyun.module.training.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.tenacitycodex.renyun.module.training.entity.AgentReport;

import java.util.Optional;

@Repository
public interface AgentReportRepository extends JpaRepository<AgentReport, Long> {

    Optional<AgentReport> findTopByPatientIdOrderByCreatedAtDesc(Long patientId);

    Optional<AgentReport> findByTrainingId(Long trainingId);
}
