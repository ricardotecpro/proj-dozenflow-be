package com.dozenflow.be.task;

import com.dozenflow.be.attachment.Attachment;
import com.dozenflow.be.attachment.AttachmentRepository;
import com.dozenflow.be.label.Label;
import com.dozenflow.be.label.LabelRepository;
import com.dozenflow.be.list.TaskList;
import com.dozenflow.be.list.TaskListRepository;
import com.dozenflow.be.task.dto.TaskRequestDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final LabelRepository labelRepository;
    private final TaskListRepository taskListRepository;
    private final AttachmentRepository attachmentRepository;

    public TaskService(TaskRepository taskRepository, LabelRepository labelRepository,
                        TaskListRepository taskListRepository, AttachmentRepository attachmentRepository) {
        this.taskRepository = taskRepository;
        this.labelRepository = labelRepository;
        this.taskListRepository = taskListRepository;
        this.attachmentRepository = attachmentRepository;
    }

    public List<Task> findAll() {
        return taskRepository.findAllByArchivedFalseOrderByTaskOrderAsc();
    }

    public List<Task> findArchived() {
        return taskRepository.findAllByArchivedTrueOrderByTaskOrderAsc();
    }

    public Task create(Task task) {
        requireListExists(task.getListId());
        return taskRepository.save(task);
    }

    @Transactional
    public Task update(Long id, TaskRequestDTO dto) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + id));

        requireListExists(dto.listId());

        existingTask.setTitle(dto.title());
        existingTask.setDescription(dto.description());
        existingTask.setListId(dto.listId());
        existingTask.setTaskOrder(dto.taskOrder());
        existingTask.setDueDate(dto.dueDate());
        existingTask.setCoverSize(dto.coverSize());
        if (dto.coverAttachmentId() != null) {
            requireOwnedAttachment(id, dto.coverAttachmentId());
            existingTask.setCoverAttachmentId(dto.coverAttachmentId());
            existingTask.setCoverColor(null);
        } else {
            existingTask.setCoverColor(dto.coverColor());
            existingTask.setCoverAttachmentId(null);
        }

        return taskRepository.save(existingTask);
    }

    @Transactional
    public Task archive(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + id));
        task.setArchived(true);
        return taskRepository.save(task);
    }

    @Transactional
    public Task restore(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + id));

        TaskList list = taskListRepository.findById(task.getListId())
                .orElseThrow(() -> new EntityNotFoundException("TaskList not found with id: " + task.getListId()));

        if (list.isArchived()) {
            list.setArchived(false);
            taskListRepository.save(list);
        }

        task.setArchived(false);
        return taskRepository.save(task);
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

    private void requireListExists(Long listId) {
        if (!taskListRepository.existsById(listId)) {
            throw new EntityNotFoundException("TaskList not found with id: " + listId);
        }
    }

    private void requireOwnedAttachment(Long taskId, Long attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new EntityNotFoundException("Attachment not found with id: " + attachmentId));
        if (!attachment.getTask().getId().equals(taskId)) {
            throw new EntityNotFoundException("Attachment not found with id: " + attachmentId);
        }
    }
}
