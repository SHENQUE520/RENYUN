package org.tenacitycodex.renyun.common.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PatientApproveRequest {
    @NotNull(message = "doctorId不能为空")
    private Long doctorId;
    @NotNull(message = "doctorName不能为空")
    private String doctorName;
}
