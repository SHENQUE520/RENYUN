package org.tenacitycodex.renyun.module.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tenacitycodex.renyun.common.dto.TaskDTO;
import org.tenacitycodex.renyun.common.util.Snowflake;
import org.tenacitycodex.renyun.module.training.repository.TaskRepository;
import org.tenacitycodex.renyun.module.user.entity.Task;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    public List<TaskDTO> getTasksByPatient(Long patientId) {
        return taskRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
                .stream()
                .map(TaskDTO::from)
                .collect(Collectors.toList());
    }

    public List<TaskDTO> getTasksByPatientAndStatus(Long patientId, String status) {
        return taskRepository.findByPatientIdAndStatusOrderByCreatedAtDesc(patientId, status)
                .stream()
                .map(TaskDTO::from)
                .collect(Collectors.toList());
    }

    public Task createTask(Long patientId, String name, Integer count, String unit,
                           String description, String source) {
        Task task = Task.builder()
                .id(Snowflake.nextId())
                .patientId(patientId)
                .name(name)
                .count(count)
                .unit(unit)
                .description(description)
                .status("pending")
                .source(source)
                .build();
        return taskRepository.save(task);
    }

    public void saveAll(List<Task> tasks) {
        taskRepository.saveAll(tasks);
    }
}
