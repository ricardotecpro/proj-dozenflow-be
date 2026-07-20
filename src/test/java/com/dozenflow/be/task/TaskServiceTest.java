package com.dozenflow.be.task;

import com.dozenflow.be.label.Label;
import com.dozenflow.be.label.LabelRepository;
import com.dozenflow.be.task.dto.TaskRequestDTO;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private LabelRepository labelRepository;

    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskService = new TaskService(taskRepository, labelRepository);
    }

    @Test
    void findAll_returnsTasksOrderedByTaskOrder() {
        Task task = new Task();
        task.setId(1L);
        when(taskRepository.findAllByOrderByTaskOrderAsc()).thenReturn(List.of(task));

        List<Task> result = taskService.findAll();

        assertThat(result).containsExactly(task);
    }

    @Test
    void create_savesAndReturnsTask() {
        Task task = new Task();
        task.setTitle("New task");
        when(taskRepository.save(task)).thenReturn(task);

        Task result = taskService.create(task);

        assertThat(result).isEqualTo(task);
        verify(taskRepository).save(task);
    }

    @Test
    void update_appliesDtoFieldsAndSaves_whenTaskExists() {
        Task existing = new Task();
        existing.setId(1L);
        existing.setTitle("Old title");
        existing.setStatus(TaskStatus.A_FAZER);
        existing.setTaskOrder(0);

        LocalDate dueDate = LocalDate.of(2026, 8, 1);
        TaskRequestDTO dto = new TaskRequestDTO("New title", "New description", TaskStatus.CONCLUIDA, 3, dueDate);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task result = taskService.update(1L, dto);

        assertThat(result.getTitle()).isEqualTo("New title");
        assertThat(result.getDescription()).isEqualTo("New description");
        assertThat(result.getStatus()).isEqualTo(TaskStatus.CONCLUIDA);
        assertThat(result.getTaskOrder()).isEqualTo(3);
        assertThat(result.getDueDate()).isEqualTo(dueDate);
    }

    @Test
    void update_throwsEntityNotFoundException_whenTaskDoesNotExist() {
        TaskRequestDTO dto = new TaskRequestDTO("Title", "Description", TaskStatus.A_FAZER, 0, null);
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.update(99L, dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");

        verify(taskRepository, never()).save(any());
    }

    @Test
    void delete_removesTask_whenTaskExists() {
        when(taskRepository.existsById(1L)).thenReturn(true);

        taskService.delete(1L);

        verify(taskRepository).deleteById(1L);
    }

    @Test
    void delete_throwsEntityNotFoundException_whenTaskDoesNotExist() {
        when(taskRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> taskService.delete(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");

        verify(taskRepository, never()).deleteById(any());
    }

    @Test
    void attachLabel_addsLabelToTaskAndSaves_whenBothExist() {
        Task task = new Task();
        task.setId(1L);
        Label label = new Label();
        label.setId(2L);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(labelRepository.findById(2L)).thenReturn(Optional.of(label));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task result = taskService.attachLabel(1L, 2L);

        assertThat(result.getLabels()).containsExactly(label);
    }

    @Test
    void attachLabel_throwsEntityNotFoundException_whenLabelDoesNotExist() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(new Task()));
        when(labelRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.attachLabel(1L, 99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");

        verify(taskRepository, never()).save(any());
    }

    @Test
    void detachLabel_removesLabelFromTaskAndSaves_whenBothExist() {
        Task task = new Task();
        task.setId(1L);
        Label label = new Label();
        label.setId(2L);
        task.getLabels().add(label);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(labelRepository.existsById(2L)).thenReturn(true);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task result = taskService.detachLabel(1L, 2L);

        assertThat(result.getLabels()).isEmpty();
    }
}
