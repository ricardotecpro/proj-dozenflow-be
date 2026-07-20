package com.dozenflow.be.task.mapper;

import com.dozenflow.be.checklist.ChecklistItem;
import com.dozenflow.be.comment.Comment;
import com.dozenflow.be.label.Label;
import com.dozenflow.be.label.mapper.LabelMapper;
import com.dozenflow.be.task.Task;
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
        TaskRequestDTO dto =
                new TaskRequestDTO("Title", "Description", 2L, 2, dueDate, "#0079bf", "FULL", null);

        Task task = mapper.toEntity(dto);

        assertThat(task.getId()).isNull();
        assertThat(task.getTitle()).isEqualTo("Title");
        assertThat(task.getDescription()).isEqualTo("Description");
        assertThat(task.getListId()).isEqualTo(2L);
        assertThat(task.getTaskOrder()).isEqualTo(2);
        assertThat(task.getDueDate()).isEqualTo(dueDate);
        assertThat(task.getCoverColor()).isEqualTo("#0079bf");
        assertThat(task.getCoverSize()).isEqualTo("FULL");
    }

    @Test
    void toResponseDTO_mapsAllFieldsFromEntity() {
        LocalDate dueDate = LocalDate.of(2026, 8, 1);
        Task task = new Task();
        task.setId(10L);
        task.setTitle("Title");
        task.setDescription("Description");
        task.setListId(3L);
        task.setTaskOrder(5);
        task.setArchived(true);
        task.setDueDate(dueDate);
        task.setCoverColor("#0079bf");
        task.setCoverSize("FULL");
        task.setCoverAttachmentId(7L);

        TaskResponseDTO dto = mapper.toResponseDTO(task);

        assertThat(dto.id()).isEqualTo(10L);
        assertThat(dto.title()).isEqualTo("Title");
        assertThat(dto.description()).isEqualTo("Description");
        assertThat(dto.listId()).isEqualTo(3L);
        assertThat(dto.taskOrder()).isEqualTo(5);
        assertThat(dto.archived()).isTrue();
        assertThat(dto.dueDate()).isEqualTo(dueDate);
        assertThat(dto.coverColor()).isEqualTo("#0079bf");
        assertThat(dto.coverSize()).isEqualTo("FULL");
        assertThat(dto.coverAttachmentId()).isEqualTo(7L);
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
        task.setListId(1L);

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
        task.setListId(1L);

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
        task.setListId(1L);
        task.getLabels().add(green);
        task.getLabels().add(yellow);

        TaskResponseDTO dto = mapper.toResponseDTO(task);

        assertThat(dto.labels()).extracting("id").containsExactly(1L, 2L);
    }
}
