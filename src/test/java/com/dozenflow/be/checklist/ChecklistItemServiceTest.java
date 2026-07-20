package com.dozenflow.be.checklist;

import com.dozenflow.be.task.Task;
import com.dozenflow.be.task.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChecklistItemServiceTest {

    @Mock
    private ChecklistItemRepository checklistItemRepository;

    @Mock
    private TaskRepository taskRepository;

    private ChecklistItemService checklistItemService;

    @BeforeEach
    void setUp() {
        checklistItemService = new ChecklistItemService(checklistItemRepository, taskRepository);
    }

    @Test
    void findAllByTaskId_returnsItemsOrderedByItemOrder() {
        ChecklistItem item = new ChecklistItem();
        when(checklistItemRepository.findByTaskIdOrderByItemOrderAsc(1L)).thenReturn(List.of(item));

        List<ChecklistItem> result = checklistItemService.findAllByTaskId(1L);

        assertThat(result).containsExactly(item);
    }

    @Test
    void create_attachesTaskAndSaves_whenTaskExists() {
        Task task = new Task();
        task.setId(1L);
        ChecklistItem item = new ChecklistItem();
        item.setTitle("New item");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(checklistItemRepository.save(item)).thenReturn(item);

        ChecklistItem result = checklistItemService.create(1L, item);

        assertThat(result.getTask()).isEqualTo(task);
        verify(checklistItemRepository).save(item);
    }

    @Test
    void create_throwsEntityNotFoundException_whenTaskDoesNotExist() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> checklistItemService.create(99L, new ChecklistItem()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void update_appliesFieldsAndSaves_whenItemBelongsToTask() {
        Task task = new Task();
        task.setId(1L);
        ChecklistItem existing = new ChecklistItem();
        existing.setId(2L);
        existing.setTask(task);
        existing.setTitle("Old");
        existing.setDone(false);
        existing.setItemOrder(0);

        ChecklistItem updated = new ChecklistItem();
        updated.setTitle("New");
        updated.setDone(true);
        updated.setItemOrder(1);

        when(checklistItemRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(checklistItemRepository.save(any(ChecklistItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChecklistItem result = checklistItemService.update(1L, 2L, updated);

        assertThat(result.getTitle()).isEqualTo("New");
        assertThat(result.isDone()).isTrue();
        assertThat(result.getItemOrder()).isEqualTo(1);
    }

    @Test
    void update_throwsEntityNotFoundException_whenItemBelongsToDifferentTask() {
        Task task = new Task();
        task.setId(1L);
        ChecklistItem existing = new ChecklistItem();
        existing.setId(2L);
        existing.setTask(task);

        when(checklistItemRepository.findById(2L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> checklistItemService.update(999L, 2L, new ChecklistItem()))
                .isInstanceOf(EntityNotFoundException.class);

        verify(checklistItemRepository, never()).save(any());
    }

    @Test
    void delete_removesItem_whenItemBelongsToTask() {
        Task task = new Task();
        task.setId(1L);
        ChecklistItem existing = new ChecklistItem();
        existing.setId(2L);
        existing.setTask(task);

        when(checklistItemRepository.findById(2L)).thenReturn(Optional.of(existing));

        checklistItemService.delete(1L, 2L);

        verify(checklistItemRepository).delete(existing);
    }

    @Test
    void delete_throwsEntityNotFoundException_whenItemDoesNotExist() {
        when(checklistItemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> checklistItemService.delete(1L, 99L))
                .isInstanceOf(EntityNotFoundException.class);

        verify(checklistItemRepository, never()).delete(any());
    }
}
