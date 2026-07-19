package com.dozenflow.be.task.mapper;

import com.dozenflow.be.checklist.ChecklistItem;
import com.dozenflow.be.label.dto.LabelResponseDTO;
import com.dozenflow.be.label.mapper.LabelMapper;
import com.dozenflow.be.task.Task;
import com.dozenflow.be.task.dto.TaskRequestDTO;
import com.dozenflow.be.task.dto.TaskResponseDTO;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class TaskMapper {

    private final LabelMapper labelMapper;

    public TaskMapper(LabelMapper labelMapper) {
        this.labelMapper = labelMapper;
    }

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
        List<LabelResponseDTO> labels = entity.getLabels().stream()
                .map(labelMapper::toResponseDTO)
                .sorted(Comparator.comparing(LabelResponseDTO::id))
                .toList();

        int checklistTotal = entity.getChecklistItems().size();
        int checklistDone = (int) entity.getChecklistItems().stream().filter(ChecklistItem::isDone).count();

        return new TaskResponseDTO(
                entity.getId(), entity.getTitle(), entity.getDescription(),
                entity.getStatus(), entity.getTaskOrder(), entity.getDueDate(), labels,
                checklistTotal, checklistDone, entity.getComments().size()
        );
    }
}