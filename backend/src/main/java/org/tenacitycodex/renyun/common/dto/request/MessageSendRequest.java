package org.tenacitycodex.renyun.common.dto.request;

import lombok.Data;

@Data
public class MessageSendRequest {
    private String id;
    private String fromRole;
    private String fromName;
    private String toPatientId;
    private String type;
    private String text;
    private String time;
    private String date;
    private Boolean read;
    private Boolean recalled;
}
