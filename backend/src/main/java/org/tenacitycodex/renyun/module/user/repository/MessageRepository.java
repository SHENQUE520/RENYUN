package org.tenacitycodex.renyun.module.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.tenacitycodex.renyun.module.user.entity.Message;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {
    List<Message> findByToPatientIdOrderByCreatedAtAsc(String toPatientId);

    List<Message> findAllByOrderByCreatedAtAsc();
}
