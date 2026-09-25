package org.tenacitycodex.renyun.common.dto;

import lombok.Builder;
import lombok.Data;
import org.tenacitycodex.renyun.module.user.entity.Message;

import java.time.LocalDateTime;

@Data
@Builder
public class MessageDTO {
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
    private LocalDateTime createdAt;

    public static MessageDTO from(Message msg) {
        if (msg == null) return null;
        return MessageDTO.builder()
                .id(msg.getId())
                .fromRole(msg.getFromRole())
                .fromName(msg.getFromName())
                .toPatientId(msg.getToPatientId())
                .type(msg.getType())
                .text(msg.getText())
                .time(msg.getTime())
                .date(msg.getDate())
                .read(msg.getRead())
                .recalled(msg.getRecalled())
                .createdAt(msg.getCreatedAt())
                .build();
    }
}
