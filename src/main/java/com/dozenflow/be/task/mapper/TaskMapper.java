package com.dozenflow.be.task.mapper;

import com.dozenflow.be.task.Task;
import com.dozenflow.be.task.dto.TaskRequestDTO;
import com.dozenflow.be.task.dto.TaskResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public Task toEntity(TaskRequestDTO dto) {
        Task task = new Task();
        task.setTitle(dto.title());
        task.setDescription(dto.description());
        task.setStatus(dto.status());
        task.setTaskOrder(dto.taskOrder());
        task.setDueDate(dto.dueDate());
        return task;
    }

    public TaskResponseDTO toResponseDTO(Task entity) {
        return new TaskResponseDTO(
                entity.getId(), entity.getTitle(), entity.getDescription(),
                entity.getStatus(), entity.getTaskOrder(), entity.getDueDate()
        );
    }
}