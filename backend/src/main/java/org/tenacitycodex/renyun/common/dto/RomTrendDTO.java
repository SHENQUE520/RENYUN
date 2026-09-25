package org.tenacitycodex.renyun.common.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class RomTrendDTO {

    private Long patientId;
    private int days;
    private List<DailyRom> points;
    private Double startRom;
    private Double endRom;
    private Double deltaRom;
    private String trend;

    @Data
    @Builder
    public static class DailyRom {
        private LocalDate date;
        private Double rom;
    }
}
