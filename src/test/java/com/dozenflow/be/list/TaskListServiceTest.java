package com.dozenflow.be.list;

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
class TaskListServiceTest {

    @Mock
    private TaskListRepository taskListRepository;

    @Mock
    private TaskRepository taskRepository;

    private TaskListService taskListService;

    @BeforeEach
    void setUp() {
        taskListService = new TaskListService(taskListRepository, taskRepository);
    }

    @Test
    void findAll_returnsActiveLists() {
        TaskList list = new TaskList();
        list.setId(1L);
        when(taskListRepository.findAllByArchivedFalseOrderByPositionAsc()).thenReturn(List.of(list));

        List<TaskList> result = taskListService.findAll();

        assertThat(result).containsExactly(list);
    }

    @Test
    void findArchived_returnsArchivedLists() {
        TaskList list = new TaskList();
        list.setId(1L);
        list.setArchived(true);
        when(taskListRepository.findAllByArchivedTrueOrderByPositionAsc()).thenReturn(List.of(list));

        List<TaskList> result = taskListService.findArchived();

        assertThat(result).containsExactly(list);
    }

    @Test
    void create_savesAndReturnsList() {
        TaskList list = new TaskList();
        list.setName("Backlog");
        when(taskListRepository.save(list)).thenReturn(list);

        TaskList result = taskListService.create(list);

        assertThat(result).isEqualTo(list);
    }

    @Test
    void update_appliesFieldsAndSaves_whenListExists() {
        TaskList existing = new TaskList();
        existing.setId(1L);
        existing.setName("Old");
        existing.setPosition(0);

        TaskList updated = new TaskList();
        updated.setName("New");
        updated.setPosition(2);

        when(taskListRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(taskListRepository.save(any(TaskList.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskList result = taskListService.update(1L, updated);

        assertThat(result.getName()).isEqualTo("New");
        assertThat(result.getPosition()).isEqualTo(2);
    }

    @Test
    void update_throwsEntityNotFoundException_whenListDoesNotExist() {
        when(taskListRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskListService.update(99L, new TaskList()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void archive_cascadesToTasks_andSetsArchivedTrue() {
        TaskList list = new TaskList();
        list.setId(1L);
        list.setArchived(false);

        when(taskListRepository.findById(1L)).thenReturn(Optional.of(list));
        when(taskListRepository.countByArchivedFalse()).thenReturn(3L);
        when(taskListRepository.save(any(TaskList.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskList result = taskListService.archive(1L);

        assertThat(result.isArchived()).isTrue();
        verify(taskRepository).archiveAllByListId(1L);
    }

    @Test
    void archive_throwsLastActiveListException_whenOnlyOneActiveListRemains() {
        TaskList list = new TaskList();
        list.setId(1L);
        list.setArchived(false);

        when(taskListRepository.findById(1L)).thenReturn(Optional.of(list));
        when(taskListRepository.countByArchivedFalse()).thenReturn(1L);

        assertThatThrownBy(() -> taskListService.archive(1L))
                .isInstanceOf(LastActiveListException.class);

        verify(taskListRepository, never()).save(any());
        verify(taskRepository, never()).archiveAllByListId(any());
    }

    @Test
    void archive_returnsNotFound_whenListDoesNotExist() {
        when(taskListRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskListService.archive(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void restore_setsArchivedFalse_whenListExists() {
        TaskList list = new TaskList();
        list.setId(1L);
        list.setArchived(true);

        when(taskListRepository.findById(1L)).thenReturn(Optional.of(list));
        when(taskListRepository.save(any(TaskList.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskList result = taskListService.restore(1L);

        assertThat(result.isArchived()).isFalse();
    }

    @Test
    void delete_removesList_whenListExistsAndIsNotTheLastActiveOne() {
        TaskList list = new TaskList();
        list.setId(1L);
        list.setArchived(false);

        when(taskListRepository.findById(1L)).thenReturn(Optional.of(list));
        when(taskListRepository.countByArchivedFalse()).thenReturn(2L);

        taskListService.delete(1L);

        verify(taskListRepository).deleteById(1L);
    }

    @Test
    void delete_throwsLastActiveListException_whenDeletingLastActiveListDirectly() {
        TaskList list = new TaskList();
        list.setId(1L);
        list.setArchived(false);

        when(taskListRepository.findById(1L)).thenReturn(Optional.of(list));
        when(taskListRepository.countByArchivedFalse()).thenReturn(1L);

        assertThatThrownBy(() -> taskListService.delete(1L))
                .isInstanceOf(LastActiveListException.class);

        verify(taskListRepository, never()).deleteById(any());
    }

    @Test
    void delete_allowsDeletingAnAlreadyArchivedList_regardlessOfActiveCount() {
        TaskList list = new TaskList();
        list.setId(1L);
        list.setArchived(true);

        when(taskListRepository.findById(1L)).thenReturn(Optional.of(list));

        taskListService.delete(1L);

        verify(taskListRepository).deleteById(1L);
        verify(taskListRepository, never()).countByArchivedFalse();
    }

    @Test
    void delete_throwsEntityNotFoundException_whenListDoesNotExist() {
        when(taskListRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskListService.delete(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");

        verify(taskListRepository, never()).deleteById(any());
    }
}
