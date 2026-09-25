package org.tenacitycodex.renyun.module.training.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tenacitycodex.renyun.common.util.Snowflake;
import org.tenacitycodex.renyun.module.training.entity.AgentReport;
import org.tenacitycodex.renyun.module.training.repository.AgentReportRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AgentReportService {

    private final AgentReportRepository agentReportRepository;

    public AgentReport save(AgentReport report) {
        if (report.getId() == null) {
            report.setId(Snowflake.nextId());
        }
        return agentReportRepository.save(report);
    }

    public Optional<AgentReport> getLatestByPatient(Long patientId) {
        return agentReportRepository.findTopByPatientIdOrderByCreatedAtDesc(patientId);
    }

    public Optional<AgentReport> getByTrainingId(Long trainingId) {
        return agentReportRepository.findByTrainingId(trainingId);
    }
}
