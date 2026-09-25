package org.tenacitycodex.renyun.module.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tenacitycodex.renyun.common.dto.RomTrendDTO;
import org.tenacitycodex.renyun.common.dto.TrainingHistoryDTO;
import org.tenacitycodex.renyun.common.dto.request.TrainingStats;
import org.tenacitycodex.renyun.common.dto.request.TrainingUploadRequest;
import org.tenacitycodex.renyun.common.exceptions.ApiException;
import org.tenacitycodex.renyun.common.util.Snowflake;
import org.tenacitycodex.renyun.module.training.repository.TrainingRecordRepository;
import org.tenacitycodex.renyun.module.user.entity.TrainingRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainingService {

    private final TrainingRecordRepository trainingRecordRepository;

    @Transactional
    public TrainingRecord uploadTraining(TrainingUploadRequest request, Long userId) {
        TrainingStats stats = request.getStats();
        TrainingRecord record = TrainingRecord.builder()
                .id(Snowflake.nextId())
                .patientId(userId)
                .mode(request.getMode())
                .modeName(request.getModeName())
                .trainingDate(LocalDate.now())
                .durationSec(request.getDurationSec())
                .samples(request.getSamples())
                .avgPitch(stats.getAvgPitch())
                .maxPitch(stats.getMaxPitch())
                .minPitch(stats.getMinPitch())
                .avgRoll(stats.getAvgRoll())
                .maxRoll(stats.getMaxRoll())
                .minRoll(stats.getMinRoll())
                .avgKed(stats.getAvgKed())
                .standardRatio(stats.getStandardRatio())
                .adjustRatio(stats.getAdjustRatio())
                .dangerRatio(stats.getDangerRatio())
                .build();
        return trainingRecordRepository.save(record);
    }

    public List<TrainingHistoryDTO> getRecentHistory(Long patientId, int limit) {
        return trainingRecordRepository
                .findByPatientIdOrderByTrainingDateDesc(patientId, PageRequest.of(0, limit))
                .stream()
                .map(TrainingHistoryDTO::from)
                .collect(Collectors.toList());
    }

    public List<TrainingHistoryDTO> getHistoryBetween(Long patientId, LocalDate start, LocalDate end) {
        return trainingRecordRepository
                .findByPatientIdAndTrainingDateBetweenOrderByTrainingDateAsc(patientId, start, end)
                .stream()
                .map(TrainingHistoryDTO::from)
                .collect(Collectors.toList());
    }

    public Optional<TrainingRecord> getLatestTraining(Long patientId) {
        return trainingRecordRepository.findTopByPatientIdOrderByTrainingDateDesc(patientId);
    }

    /**
     * 以 avgKed（膝关节夹角均值）作为 ROM 趋势指标。
     */
    public RomTrendDTO getRomTrend(Long patientId, int days) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(days - 1L);
        List<TrainingRecord> records = trainingRecordRepository
                .findByPatientIdAndTrainingDateBetweenOrderByTrainingDateAsc(patientId, start, end);

        List<RomTrendDTO.DailyRom> points = records.stream()
                .filter(r -> r.getAvgKed() != null)
                .map(r -> RomTrendDTO.DailyRom.builder()
                        .date(r.getTrainingDate())
                        .rom(r.getAvgKed())
                        .build())
                .collect(Collectors.toList());

        Double startRom = points.isEmpty() ? null : points.get(0).getRom();
        Double endRom = points.isEmpty() ? null : points.get(points.size() - 1).getRom();
        Double deltaRom = (startRom != null && endRom != null) ? endRom - startRom : null;

        String trend;
        if (deltaRom == null) {
            trend = "NO_DATA";
        } else if (deltaRom > 2) {
            trend = "IMPROVING";
        } else if (deltaRom < -2) {
            trend = "DECLINING";
        } else {
            trend = "STABLE";
        }

        return RomTrendDTO.builder()
                .patientId(patientId)
                .days(days)
                .points(points)
                .startRom(startRom)
                .endRom(endRom)
                .deltaRom(deltaRom)
                .trend(trend)
                .build();
    }

    public TrainingRecord getById(Long trainingId) {
        return trainingRecordRepository.findById(trainingId)
                .orElseThrow(() -> new ApiException(org.springframework.http.HttpStatus.NOT_FOUND, "训练记录不存在"));
    }
}
