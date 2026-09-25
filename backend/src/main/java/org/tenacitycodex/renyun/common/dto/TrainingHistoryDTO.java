package org.tenacitycodex.renyun.common.dto;

import lombok.Builder;
import lombok.Data;
import org.tenacitycodex.renyun.module.user.entity.TrainingRecord;

import java.time.LocalDate;

@Data
@Builder
public class TrainingHistoryDTO {

    private Long id;
    private String mode;
    private String modeName;
    private LocalDate trainingDate;
    private Double durationSec;
    private Integer samples;
    private Double avgPitch;
    private Double maxPitch;
    private Double minPitch;
    private Double avgRoll;
    private Double maxRoll;
    private Double minRoll;
    private Double avgKed;
    private Double standardRatio;
    private Double adjustRatio;
    private Double dangerRatio;

    public static TrainingHistoryDTO from(TrainingRecord r) {
        return TrainingHistoryDTO.builder()
                .id(r.getId())
                .mode(r.getMode())
                .modeName(r.getModeName())
                .trainingDate(r.getTrainingDate())
                .durationSec(r.getDurationSec())
                .samples(r.getSamples())
                .avgPitch(r.getAvgPitch())
                .maxPitch(r.getMaxPitch())
                .minPitch(r.getMinPitch())
                .avgRoll(r.getAvgRoll())
                .maxRoll(r.getMaxRoll())
                .minRoll(r.getMinRoll())
                .avgKed(r.getAvgKed())
                .standardRatio(r.getStandardRatio())
                .adjustRatio(r.getAdjustRatio())
                .dangerRatio(r.getDangerRatio())
                .build();
    }
}
