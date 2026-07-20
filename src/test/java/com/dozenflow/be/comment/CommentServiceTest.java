package com.dozenflow.be.comment;

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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private TaskRepository taskRepository;

    private CommentService commentService;

    @BeforeEach
    void setUp() {
        commentService = new CommentService(commentRepository, taskRepository);
    }

    @Test
    void findAllByTaskId_returnsCommentsOrderedByCreatedAt() {
        Comment comment = new Comment();
        when(commentRepository.findByTaskIdOrderByCreatedAtAsc(1L)).thenReturn(List.of(comment));

        List<Comment> result = commentService.findAllByTaskId(1L);

        assertThat(result).containsExactly(comment);
    }

    @Test
    void create_attachesTaskAndSaves_whenTaskExists() {
        Task task = new Task();
        task.setId(1L);
        Comment comment = new Comment();
        comment.setBody("Nice work");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(commentRepository.save(comment)).thenReturn(comment);

        Comment result = commentService.create(1L, comment);

        assertThat(result.getTask()).isEqualTo(task);
        verify(commentRepository).save(comment);
    }

    @Test
    void create_throwsEntityNotFoundException_whenTaskDoesNotExist() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.create(99L, new Comment()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void delete_removesComment_whenCommentBelongsToTask() {
        Task task = new Task();
        task.setId(1L);
        Comment comment = new Comment();
        comment.setId(2L);
        comment.setTask(task);

        when(commentRepository.findById(2L)).thenReturn(Optional.of(comment));

        commentService.delete(1L, 2L);

        verify(commentRepository).delete(comment);
    }

    @Test
    void delete_throwsEntityNotFoundException_whenCommentBelongsToDifferentTask() {
        Task task = new Task();
        task.setId(1L);
        Comment comment = new Comment();
        comment.setId(2L);
        comment.setTask(task);

        when(commentRepository.findById(2L)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.delete(999L, 2L))
                .isInstanceOf(EntityNotFoundException.class);

        verify(commentRepository, never()).delete(any());
    }

    @Test
    void delete_throwsEntityNotFoundException_whenCommentDoesNotExist() {
        when(commentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.delete(1L, 99L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
