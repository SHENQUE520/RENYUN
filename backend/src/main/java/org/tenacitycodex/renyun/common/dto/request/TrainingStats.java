package org.tenacitycodex.renyun.common.dto.request;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TrainingStats {
    private Integer totalReps;
    private Integer validReps;
    private Double avgRom;
    private Double maxRom;
    private Double minRom;
    private Double avgKed;
    private Double maxKed;
    private Double stability;
    private Double passRate;
    private List<Map<String, Object>> repDetails;
}
