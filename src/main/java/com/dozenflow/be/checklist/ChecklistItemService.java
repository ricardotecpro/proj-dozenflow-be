package com.dozenflow.be.checklist;

import com.dozenflow.be.task.Task;
import com.dozenflow.be.task.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChecklistItemService {

    private final ChecklistItemRepository checklistItemRepository;
    private final TaskRepository taskRepository;

    public ChecklistItemService(ChecklistItemRepository checklistItemRepository, TaskRepository taskRepository) {
        this.checklistItemRepository = checklistItemRepository;
        this.taskRepository = taskRepository;
    }

    public List<ChecklistItem> findAllByTaskId(Long taskId) {
        return checklistItemRepository.findByTaskIdOrderByItemOrderAsc(taskId);
    }

    public ChecklistItem create(Long taskId, ChecklistItem item) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + taskId));
        item.setTask(task);
        return checklistItemRepository.save(item);
    }

    @Transactional
    public ChecklistItem update(Long taskId, Long itemId, ChecklistItem updated) {
        ChecklistItem existing = findOwnedItem(taskId, itemId);
        existing.setTitle(updated.getTitle());
        existing.setDone(updated.isDone());
        existing.setItemOrder(updated.getItemOrder());
        return checklistItemRepository.save(existing);
    }

    public void delete(Long taskId, Long itemId) {
        ChecklistItem existing = findOwnedItem(taskId, itemId);
        checklistItemRepository.delete(existing);
    }

    private ChecklistItem findOwnedItem(Long taskId, Long itemId) {
        ChecklistItem item = checklistItemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Checklist item not found with id: " + itemId));
        if (!item.getTask().getId().equals(taskId)) {
            throw new EntityNotFoundException("Checklist item not found with id: " + itemId);
        }
        return item;
    }
}
