package org.tenacitycodex.renyun.common.dto.response;

import lombok.Builder;
import lombok.Data;
import org.tenacitycodex.renyun.common.dto.TaskDTO;

import java.util.List;

@Data
@Builder
public class TrainingUploadResponse {

    private Long trainingId;
    private AgentReportDTO report;
    private List<TaskDTO> suggestedTasks;
}
