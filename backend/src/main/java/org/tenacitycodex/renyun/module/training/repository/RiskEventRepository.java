package org.tenacitycodex.renyun.module.training.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.stereotype.Repository;
import org.tenacitycodex.renyun.module.training.entity.RiskEvent;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RedisHash
public interface RiskEventRepository extends JpaRepository<RiskEvent, Long> {

    List<RiskEvent> findByPatientIdOrderByCreatedAtDesc(Long patientId);

    List<RiskEvent> findByPatientIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long patientId, LocalDateTime start, LocalDateTime end);

    List<RiskEvent> findByTrainingId(Long trainingId);
}
