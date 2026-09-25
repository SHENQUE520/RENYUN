package org.tenacitycodex.renyun.api.v1.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.tenacitycodex.renyun.common.dto.ApiResponse;
import org.tenacitycodex.renyun.common.dto.MessageDTO;
import org.tenacitycodex.renyun.common.dto.request.MessageSendRequest;
import org.tenacitycodex.renyun.common.service.SseEventService;
import org.tenacitycodex.renyun.module.user.service.MessageService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final SseEventService sseEventService;

    @GetMapping("/stream")
    public SseEmitter stream() {
        return sseEventService.createEmitter();
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MessageDTO>>> getMessages(
            @RequestParam(name = "patientId", required = false) String patientId) {
        List<MessageDTO> messages = patientId != null
                ? messageService.getMessagesByPatient(patientId)
                : messageService.getAllMessages();
        return ResponseEntity.ok(ApiResponse.ok(messages));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MessageDTO>> sendMessage(@RequestBody MessageSendRequest request) {
        MessageDTO dto = MessageDTO.builder()
                .id(request.getId())
                .fromRole(request.getFromRole())
                .fromName(request.getFromName())
                .toPatientId(request.getToPatientId())
                .type(request.getType())
                .text(request.getText())
                .time(request.getTime())
                .date(request.getDate())
                .read(request.getRead())
                .recalled(request.getRecalled())
                .build();
        MessageDTO saved = messageService.upsertMessage(dto);
        sseEventService.broadcast("{\"type\":\"message\",\"msg\":" + toJson(saved) + "}");
        return ResponseEntity.ok(ApiResponse.ok(saved));
    }

    @PatchMapping("/{id}/recall")
    public ResponseEntity<ApiResponse<MessageDTO>> recallMessage(@PathVariable String id) {
        MessageDTO recalled = messageService.recallMessage(id);
        sseEventService.broadcast("{\"type\":\"recall\",\"id\":\"" + safe(id) + "\"}");
        return ResponseEntity.ok(ApiResponse.ok(recalled));
    }

    private String toJson(MessageDTO msg) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"id\":\"").append(safe(msg.getId())).append("\"");
        sb.append(",\"fromRole\":\"").append(safe(msg.getFromRole())).append("\"");
        sb.append(",\"fromName\":\"").append(safe(msg.getFromName())).append("\"");
        sb.append(",\"toPatientId\":\"").append(safe(msg.getToPatientId())).append("\"");
        sb.append(",\"type\":\"").append(safe(msg.getType())).append("\"");
        sb.append(",\"text\":\"").append(safe(msg.getText())).append("\"");
        sb.append(",\"time\":\"").append(safe(msg.getTime())).append("\"");
        sb.append(",\"date\":\"").append(safe(msg.getDate())).append("\"");
        sb.append(",\"read\":").append(msg.getRead() != null && msg.getRead());
        sb.append(",\"recalled\":").append(msg.getRecalled() != null && msg.getRecalled());
        sb.append("}");
        return sb.toString();
    }

    private String safe(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
