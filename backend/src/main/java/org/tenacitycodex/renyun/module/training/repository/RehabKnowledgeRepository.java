package org.tenacitycodex.renyun.module.training.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.tenacitycodex.renyun.module.training.entity.RehabKnowledge;

import java.util.List;

@Repository
public interface RehabKnowledgeRepository extends JpaRepository<RehabKnowledge, Long> {

    List<RehabKnowledge> findByCategory(String category);

    @Query("SELECT k FROM RehabKnowledge k WHERE k.category = :category OR k.keywords LIKE %:keyword%")
    List<RehabKnowledge> searchByKeyword(@Param("category") String category, @Param("keyword") String keyword);
}
