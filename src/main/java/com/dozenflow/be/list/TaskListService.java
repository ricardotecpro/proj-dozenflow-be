package com.dozenflow.be.list;

import com.dozenflow.be.task.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskListService {

    private final TaskListRepository taskListRepository;
    private final TaskRepository taskRepository;

    public TaskListService(TaskListRepository taskListRepository, TaskRepository taskRepository) {
        this.taskListRepository = taskListRepository;
        this.taskRepository = taskRepository;
    }

    public List<TaskList> findAll() {
        return taskListRepository.findAllByArchivedFalseOrderByPositionAsc();
    }

    public List<TaskList> findArchived() {
        return taskListRepository.findAllByArchivedTrueOrderByPositionAsc();
    }

    public TaskList create(TaskList list) {
        return taskListRepository.save(list);
    }

    @Transactional
    public TaskList update(Long id, TaskList updated) {
        TaskList existing = taskListRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("TaskList not found with id: " + id));

        existing.setName(updated.getName());
        existing.setPosition(updated.getPosition());

        return taskListRepository.save(existing);
    }

    @Transactional
    public TaskList archive(Long id) {
        TaskList list = taskListRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("TaskList not found with id: " + id));

        if (!list.isArchived() && taskListRepository.countByArchivedFalse() <= 1) {
            throw new LastActiveListException("Não é possível arquivar a última lista do board.");
        }

        list.setArchived(true);
        TaskList saved = taskListRepository.save(list);
        taskRepository.archiveAllByListId(id);
        return saved;
    }

    @Transactional
    public TaskList restore(Long id) {
        TaskList list = taskListRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("TaskList not found with id: " + id));

        list.setArchived(false);
        return taskListRepository.save(list);
    }

    @Transactional
    public void delete(Long id) {
        TaskList list = taskListRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("TaskList not found with id: " + id));

        if (!list.isArchived() && taskListRepository.countByArchivedFalse() <= 1) {
            throw new LastActiveListException("Não é possível excluir a última lista do board.");
        }

        taskListRepository.deleteById(id);
    }
}
