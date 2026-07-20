package com.dozenflow.be.task;

import com.dozenflow.be.attachment.Attachment;
import com.dozenflow.be.attachment.AttachmentRepository;
import com.dozenflow.be.label.Label;
import com.dozenflow.be.label.LabelRepository;
import com.dozenflow.be.list.TaskList;
import com.dozenflow.be.list.TaskListRepository;
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

    @Mock
    private TaskListRepository taskListRepository;

    @Mock
    private AttachmentRepository attachmentRepository;

    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskService = new TaskService(taskRepository, labelRepository, taskListRepository, attachmentRepository);
    }

    @Test
    void findAll_returnsActiveTasksOrderedByTaskOrder() {
        Task task = new Task();
        task.setId(1L);
        when(taskRepository.findAllByArchivedFalseOrderByTaskOrderAsc()).thenReturn(List.of(task));

        List<Task> result = taskService.findAll();

        assertThat(result).containsExactly(task);
    }

    @Test
    void findArchived_returnsArchivedTasksOrderedByTaskOrder() {
        Task task = new Task();
        task.setId(1L);
        task.setArchived(true);
        when(taskRepository.findAllByArchivedTrueOrderByTaskOrderAsc()).thenReturn(List.of(task));

        List<Task> result = taskService.findArchived();

        assertThat(result).containsExactly(task);
    }

    @Test
    void create_savesAndReturnsTask_whenListExists() {
        Task task = new Task();
        task.setTitle("New task");
        task.setListId(1L);
        when(taskListRepository.existsById(1L)).thenReturn(true);
        when(taskRepository.save(task)).thenReturn(task);

        Task result = taskService.create(task);

        assertThat(result).isEqualTo(task);
        verify(taskRepository).save(task);
    }

    @Test
    void create_throwsEntityNotFoundException_whenListDoesNotExist() {
        Task task = new Task();
        task.setListId(99L);
        when(taskListRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> taskService.create(task))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");

        verify(taskRepository, never()).save(any());
    }

    @Test
    void update_appliesDtoFieldsAndSaves_whenTaskAndListExist() {
        Task existing = new Task();
        existing.setId(1L);
        existing.setTitle("Old title");
        existing.setListId(1L);
        existing.setTaskOrder(0);

        LocalDate dueDate = LocalDate.of(2026, 8, 1);
        TaskRequestDTO dto =
                new TaskRequestDTO("New title", "New description", 3L, 3, dueDate, "#0079bf", "FULL", null);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(taskListRepository.existsById(3L)).thenReturn(true);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task result = taskService.update(1L, dto);

        assertThat(result.getTitle()).isEqualTo("New title");
        assertThat(result.getDescription()).isEqualTo("New description");
        assertThat(result.getListId()).isEqualTo(3L);
        assertThat(result.getTaskOrder()).isEqualTo(3);
        assertThat(result.getDueDate()).isEqualTo(dueDate);
        assertThat(result.getCoverColor()).isEqualTo("#0079bf");
        assertThat(result.getCoverSize()).isEqualTo("FULL");
    }

    @Test
    void update_throwsEntityNotFoundException_whenTaskDoesNotExist() {
        TaskRequestDTO dto = new TaskRequestDTO("Title", "Description", 1L, 0, null, null, null, null);
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.update(99L, dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");

        verify(taskRepository, never()).save(any());
    }

    @Test
    void update_throwsEntityNotFoundException_whenListDoesNotExist() {
        Task existing = new Task();
        existing.setId(1L);
        existing.setListId(1L);
        TaskRequestDTO dto = new TaskRequestDTO("Title", "Description", 99L, 0, null, null, null, null);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(taskListRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> taskService.update(1L, dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");

        verify(taskRepository, never()).save(any());
    }

    @Test
    void update_setsCoverAttachmentAndClearsCoverColor_whenAttachmentBelongsToTask() {
        Task existing = new Task();
        existing.setId(1L);
        existing.setListId(1L);
        existing.setCoverColor("#0079bf");

        Task owningTask = new Task();
        owningTask.setId(1L);
        Attachment attachment = new Attachment();
        attachment.setId(5L);
        attachment.setTask(owningTask);

        TaskRequestDTO dto = new TaskRequestDTO("Title", "Description", 1L, 0, null, null, null, 5L);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(taskListRepository.existsById(1L)).thenReturn(true);
        when(attachmentRepository.findById(5L)).thenReturn(Optional.of(attachment));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task result = taskService.update(1L, dto);

        assertThat(result.getCoverAttachmentId()).isEqualTo(5L);
        assertThat(result.getCoverColor()).isNull();
    }

    @Test
    void update_throwsEntityNotFoundException_whenCoverAttachmentBelongsToAnotherTask() {
        Task existing = new Task();
        existing.setId(1L);
        existing.setListId(1L);

        Task otherTask = new Task();
        otherTask.setId(2L);
        Attachment attachment = new Attachment();
        attachment.setId(5L);
        attachment.setTask(otherTask);

        TaskRequestDTO dto = new TaskRequestDTO("Title", "Description", 1L, 0, null, null, null, 5L);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(taskListRepository.existsById(1L)).thenReturn(true);
        when(attachmentRepository.findById(5L)).thenReturn(Optional.of(attachment));

        assertThatThrownBy(() -> taskService.update(1L, dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("5");

        verify(taskRepository, never()).save(any());
    }

    @Test
    void archive_setsArchivedTrueAndSaves_whenTaskExists() {
        Task task = new Task();
        task.setId(1L);
        task.setArchived(false);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task result = taskService.archive(1L);

        assertThat(result.isArchived()).isTrue();
    }

    @Test
    void archive_throwsEntityNotFoundException_whenTaskDoesNotExist() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.archive(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void restore_setsArchivedFalse_whenParentListIsNotArchived() {
        Task task = new Task();
        task.setId(1L);
        task.setListId(1L);
        task.setArchived(true);

        TaskList list = new TaskList();
        list.setId(1L);
        list.setArchived(false);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskListRepository.findById(1L)).thenReturn(Optional.of(list));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task result = taskService.restore(1L);

        assertThat(result.isArchived()).isFalse();
        verify(taskListRepository, never()).save(any());
    }

    @Test
    void restore_alsoRestoresParentList_whenParentListIsArchived() {
        Task task = new Task();
        task.setId(1L);
        task.setListId(1L);
        task.setArchived(true);

        TaskList list = new TaskList();
        list.setId(1L);
        list.setArchived(true);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskListRepository.findById(1L)).thenReturn(Optional.of(list));
        when(taskListRepository.save(any(TaskList.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task result = taskService.restore(1L);

        assertThat(result.isArchived()).isFalse();
        assertThat(list.isArchived()).isFalse();
        verify(taskListRepository).save(list);
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
