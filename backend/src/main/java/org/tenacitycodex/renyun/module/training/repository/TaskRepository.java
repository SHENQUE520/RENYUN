package org.tenacitycodex.renyun.module.training.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.tenacitycodex.renyun.module.user.entity.Task;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByPatientIdOrderByCreatedAtDesc(Long patientId);

    List<Task> findByPatientIdAndStatusOrderByCreatedAtDesc(Long patientId, String status);

    Optional<Task> findByPatientIdAndName(Long patientId, String name);
}
