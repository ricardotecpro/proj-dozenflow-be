package com.dozenflow.be.task;

import com.dozenflow.be.label.Label;
import com.dozenflow.be.label.LabelRepository;
import com.dozenflow.be.task.dto.TaskRequestDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final LabelRepository labelRepository;

    public TaskService(TaskRepository taskRepository, LabelRepository labelRepository) {
        this.taskRepository = taskRepository;
        this.labelRepository = labelRepository;
    }

    public List<Task> findAll() {
        return taskRepository.findAllByOrderByTaskOrderAsc();
    }

    public Task create(Task task) {
        return taskRepository.save(task);
    }

    @Transactional
    public Task update(Long id, TaskRequestDTO dto) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + id));

        existingTask.setTitle(dto.title());
        existingTask.setDescription(dto.description());
        existingTask.setStatus(dto.status());
        existingTask.setTaskOrder(dto.taskOrder());
        existingTask.setDueDate(dto.dueDate());

        return taskRepository.save(existingTask);
    }

    public void delete(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new EntityNotFoundException("Task not found with id: " + id);
        }
        taskRepository.deleteById(id);
    }

    @Transactional
    public Task attachLabel(Long taskId, Long labelId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + taskId));
        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new EntityNotFoundException("Label not found with id: " + labelId));

        task.getLabels().add(label);
        return taskRepository.save(task);
    }

    @Transactional
    public Task detachLabel(Long taskId, Long labelId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + taskId));
        if (!labelRepository.existsById(labelId)) {
            throw new EntityNotFoundException("Label not found with id: " + labelId);
        }

        task.getLabels().removeIf(label -> label.getId().equals(labelId));
        return taskRepository.save(task);
    }
}