package org.tenacitycodex.renyun.module.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tenacitycodex.renyun.common.dto.MessageDTO;
import org.tenacitycodex.renyun.common.exceptions.ApiException;
import org.tenacitycodex.renyun.module.user.entity.Message;
import org.tenacitycodex.renyun.module.user.repository.MessageRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;

    public List<MessageDTO> getMessagesByPatient(String patientId) {
        return messageRepository.findByToPatientIdOrderByCreatedAtAsc(patientId)
                .stream()
                .map(MessageDTO::from)
                .collect(Collectors.toList());
    }

    public List<MessageDTO> getAllMessages() {
        return messageRepository.findAllByOrderByCreatedAtAsc()
                .stream()
                .map(MessageDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public MessageDTO upsertMessage(MessageDTO dto) {
        if (dto.getId() == null || dto.getId().isBlank()) {
            dto.setId(UUID.randomUUID().toString());
        }
        Message msg = Message.builder()
                .id(dto.getId())
                .fromRole(dto.getFromRole() != null ? dto.getFromRole() : "")
                .fromName(dto.getFromName() != null ? dto.getFromName() : "")
                .toPatientId(dto.getToPatientId() != null ? dto.getToPatientId() : "")
                .type(dto.getType() != null ? dto.getType() : "text")
                .text(dto.getText() != null ? dto.getText() : "")
                .time(dto.getTime() != null ? dto.getTime() : "")
                .date(dto.getDate() != null ? dto.getDate() : "")
                .read(dto.getRead() != null && dto.getRead())
                .recalled(dto.getRecalled() != null && dto.getRecalled())
                .build();
        messageRepository.save(msg);
        return MessageDTO.from(msg);
    }

    @Transactional
    public MessageDTO recallMessage(String id) {
        Message msg = messageRepository.findById(id)
                .orElseThrow(() -> new ApiException(org.springframework.http.HttpStatus.NOT_FOUND, "消息不存在"));
        msg.setRecalled(true);
        msg.setText("");
        messageRepository.save(msg);
        return MessageDTO.from(msg);
    }
}
