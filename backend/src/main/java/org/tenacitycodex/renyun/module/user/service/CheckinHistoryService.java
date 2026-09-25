package org.tenacitycodex.renyun.module.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tenacitycodex.renyun.module.user.entity.CheckinHistory;
import org.tenacitycodex.renyun.module.user.repository.CheckinHistoryRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CheckinHistoryService {

    private final CheckinHistoryRepository checkinHistoryRepository;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public List<CheckinHistory> getHistory(Long patientId) {
        return checkinHistoryRepository.findByPatientIdOrderByDateDesc(patientId);
    }

    public int getTotalCheckins(Long patientId) {
        return (int) checkinHistoryRepository.findByPatientIdOrderByDateDesc(patientId)
                .stream()
                .filter(CheckinHistory::getDone)
                .count();
    }

    public int getStreakDays(Long patientId) {
        List<CheckinHistory> history = checkinHistoryRepository.findByPatientIdOrderByDateDesc(patientId);
        Set<String> doneDates = new HashSet<>();
        for (CheckinHistory h : history) {
            if (Boolean.TRUE.equals(h.getDone())) {
                doneDates.add(h.getDate());
            }
        }
        int streak = 0;
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 365; i++) {
            LocalDate d = today.minusDays(i);
            if (doneDates.contains(d.format(DATE_FMT))) {
                streak++;
            } else {
                break;
            }
        }
        return streak;
    }
}
