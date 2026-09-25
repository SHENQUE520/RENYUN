package org.tenacitycodex.renyun.module.training.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tenacitycodex.renyun.common.dto.RiskEventDTO;
import org.tenacitycodex.renyun.common.util.Snowflake;
import org.tenacitycodex.renyun.module.training.entity.RiskEvent;
import org.tenacitycodex.renyun.module.training.repository.RiskEventRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RiskEventService {

    private final RiskEventRepository riskEventRepository;

    public List<RiskEventDTO> getRecentRiskEvents(Long patientId, int days) {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(days);
        return riskEventRepository
                .findByPatientIdAndCreatedAtBetweenOrderByCreatedAtDesc(patientId, start, end)
                .stream()
                .map(RiskEventDTO::from)
                .collect(Collectors.toList());
    }

    public List<RiskEventDTO> getByTrainingId(Long trainingId) {
        return riskEventRepository.findByTrainingId(trainingId)
                .stream()
                .map(RiskEventDTO::from)
                .collect(Collectors.toList());
    }

    public RiskEvent createRiskEvent(Long trainingId, Long patientId, String eventType,
                                     String severity, String description) {
        RiskEvent event = RiskEvent.builder()
                .id(Snowflake.nextId())
                .trainingId(trainingId)
                .patientId(patientId)
                .eventType(eventType)
                .severity(severity)
                .description(description)
                .build();
        return riskEventRepository.save(event);
    }
}
