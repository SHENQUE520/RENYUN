package org.tenacitycodex.renyun.module.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tenacitycodex.renyun.common.dto.TaskDTO;
import org.tenacitycodex.renyun.common.exceptions.ApiException;
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

    @Transactional
    public TaskDTO upsertTaskByName(Long patientId, String name, Integer count, String unit,
                                    String keyPoints, String details) {
        if (name == null || name.isBlank()) {
            throw new ApiException(org.springframework.http.HttpStatus.BAD_REQUEST, "缺少任务名称");
        }
        String description = (keyPoints != null ? keyPoints : "")
                + (details != null ? (keyPoints != null ? "\n" + details : details) : "");
        Task existing = taskRepository.findByPatientIdAndName(patientId, name).orElse(null);
        if (existing != null) {
            existing.setCount(count != null ? count : 10);
            existing.setUnit(unit != null ? unit : "次");
            existing.setDescription(description);
            existing.setStatus("pending");
            return TaskDTO.from(taskRepository.save(existing));
        }
        Task task = Task.builder()
                .id(Snowflake.nextId())
                .patientId(patientId)
                .name(name)
                .count(count != null ? count : 10)
                .unit(unit != null ? unit : "次")
                .description(description)
                .status("pending")
                .source("doctor")
                .build();
        return TaskDTO.from(taskRepository.save(task));
    }

    @Transactional
    public TaskDTO updateTaskStatus(Long taskId, Boolean done, String status) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ApiException(org.springframework.http.HttpStatus.NOT_FOUND, "任务不存在"));
        if (done != null) {
            task.setStatus(done ? "done" : "pending");
        }
        if (status != null) {
            task.setStatus(status);
        }
        return TaskDTO.from(taskRepository.save(task));
    }
}
