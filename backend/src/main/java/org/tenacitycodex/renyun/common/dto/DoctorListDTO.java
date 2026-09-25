package org.tenacitycodex.renyun.common.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DoctorListDTO {
    private Long id;
    private String name;
    private String hospital;
    private String department;
    private String title;
}
