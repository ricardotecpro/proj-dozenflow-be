package com.dozenflow.be.task.mapper;

import com.dozenflow.be.task.Task;
import com.dozenflow.be.task.TaskStatus;
import com.dozenflow.be.task.dto.TaskRequestDTO;
import com.dozenflow.be.task.dto.TaskResponseDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class TaskMapperTest {

    private final TaskMapper mapper = new TaskMapper();

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
    }
}
