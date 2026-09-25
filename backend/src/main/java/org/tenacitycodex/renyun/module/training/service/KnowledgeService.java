package org.tenacitycodex.renyun.module.training.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tenacitycodex.renyun.module.training.entity.RehabKnowledge;
import org.tenacitycodex.renyun.module.training.repository.RehabKnowledgeRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private final RehabKnowledgeRepository rehabKnowledgeRepository;

    public List<RehabKnowledge> searchKnowledge(String category, String keyword) {
        return rehabKnowledgeRepository.searchByKeyword(category, keyword);
    }

    public List<RehabKnowledge> getByCategory(String category) {
        return rehabKnowledgeRepository.findByCategory(category);
    }

    public String getKnowledgeSnippet(String category, String keyword) {
        List<RehabKnowledge> results = searchKnowledge(category, keyword);
        if (results.isEmpty()) {
            return "暂无相关康复知识。";
        }
        return results.stream()
                .map(k -> "[" + k.getCategory() + "] " + k.getTitle() + ": " + k.getContent())
                .collect(Collectors.joining("\n"));
    }
}
