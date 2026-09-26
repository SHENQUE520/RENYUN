package org.tenacitycodex.renyun.common.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MessageSendRequest {
    private String id;
    @NotNull(message = "fromRole不能为空")
    private String fromRole;
    @NotNull(message = "fromName不能为空")
    private String fromName;
    @NotNull(message = "toPatientId不能为空")
    private String toPatientId;
    @NotNull(message = "type不能为空")
    private String type;
    @NotNull(message = "text不能为空")
    private String text;
    @NotNull(message = "time不能为空")
    private String time;
    @NotNull(message = "date不能为空")
    private String date;
    private Boolean read;
    private Boolean recalled;
}
