package org.tenacitycodex.renyun.common.dto.request;

import lombok.Data;

@Data
public class PatientApproveRequest {
    private Long doctorId;
    private String doctorName;
}
