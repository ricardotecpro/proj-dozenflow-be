package com.dozenflow.be.task.mapper;

import com.dozenflow.be.checklist.ChecklistItem;
import com.dozenflow.be.comment.Comment;
import com.dozenflow.be.label.Label;
import com.dozenflow.be.label.mapper.LabelMapper;
import com.dozenflow.be.task.Task;
import com.dozenflow.be.task.TaskStatus;
import com.dozenflow.be.task.dto.TaskRequestDTO;
import com.dozenflow.be.task.dto.TaskResponseDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class TaskMapperTest {

    private final TaskMapper mapper = new TaskMapper(new LabelMapper());

    @Test
    void toEntity_mapsAllFieldsFromRequestDto() {
        LocalDate dueDate = LocalDate.of(2026, 8, 1);
        TaskRequestDTO dto = new TaskRequestDTO("Title", "Description", TaskStatus.EM_ANDAMENTO, 2, dueDate);

        Task task = mapper.toEntity(dto);

        assertThat(task.getId()).isNull();
        assertThat(task.getTitle()).isEqualTo("Title");
        assertThat(task.getDescription()).isEqualTo("Description");
        assertThat(task.getStatus()).isEqualTo(TaskStatus.EM_ANDAMENTO);
        assertThat(task.getTaskOrder()).isEqualTo(2);
        assertThat(task.getDueDate()).isEqualTo(dueDate);
    }

    @Test
    void toResponseDTO_mapsAllFieldsFromEntity() {
        LocalDate dueDate = LocalDate.of(2026, 8, 1);
        Task task = new Task();
        task.setId(10L);
        task.setTitle("Title");
        task.setDescription("Description");
        task.setStatus(TaskStatus.CONCLUIDA);
        task.setTaskOrder(5);
        task.setDueDate(dueDate);

        TaskResponseDTO dto = mapper.toResponseDTO(task);

        assertThat(dto.id()).isEqualTo(10L);
        assertThat(dto.title()).isEqualTo("Title");
        assertThat(dto.description()).isEqualTo("Description");
        assertThat(dto.status()).isEqualTo(TaskStatus.CONCLUIDA);
        assertThat(dto.taskOrder()).isEqualTo(5);
        assertThat(dto.dueDate()).isEqualTo(dueDate);
        assertThat(dto.labels()).isEmpty();
        assertThat(dto.checklistTotal()).isZero();
        assertThat(dto.checklistDone()).isZero();
        assertThat(dto.commentCount()).isZero();
        assertThat(dto.attachmentCount()).isZero();
    }

    @Test
    void toResponseDTO_computesCommentCountFromComments() {
        Task task = new Task();
        task.setId(10L);
        task.setTitle("Title");
        task.setStatus(TaskStatus.A_FAZER);

        Comment first = new Comment();
        first.setBody("First");
        Comment second = new Comment();
        second.setBody("Second");
        task.getComments().add(first);
        task.getComments().add(second);

        TaskResponseDTO dto = mapper.toResponseDTO(task);

        assertThat(dto.commentCount()).isEqualTo(2);
    }

    @Test
    void toResponseDTO_computesChecklistTotalsFromItems() {
        Task task = new Task();
        task.setId(10L);
        task.setTitle("Title");
        task.setStatus(TaskStatus.A_FAZER);

        ChecklistItem done = new ChecklistItem();
        done.setTitle("Done item");
        done.setDone(true);
        ChecklistItem pending = new ChecklistItem();
        pending.setTitle("Pending item");
        pending.setDone(false);
        task.getChecklistItems().add(done);
        task.getChecklistItems().add(pending);

        TaskResponseDTO dto = mapper.toResponseDTO(task);

        assertThat(dto.checklistTotal()).isEqualTo(2);
        assertThat(dto.checklistDone()).isEqualTo(1);
    }

    @Test
    void toResponseDTO_mapsLabelsSortedById() {
        Label green = new Label();
        green.setId(2L);
        green.setColorHex("#61bd4f");
        Label yellow = new Label();
        yellow.setId(1L);
        yellow.setColorHex("#f2d600");

        Task task = new Task();
        task.setId(10L);
        task.setTitle("Title");
        task.setStatus(TaskStatus.A_FAZER);
        task.getLabels().add(green);
        task.getLabels().add(yellow);

        TaskResponseDTO dto = mapper.toResponseDTO(task);

        assertThat(dto.labels()).extracting("id").containsExactly(1L, 2L);
    }
}
