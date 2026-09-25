package org.tenacitycodex.renyun.module.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.tenacitycodex.renyun.module.user.entity.CheckinHistory;

import java.util.List;

@Repository
public interface CheckinHistoryRepository extends JpaRepository<CheckinHistory, String> {
    List<CheckinHistory> findByPatientIdOrderByDateDesc(Long patientId);
}
