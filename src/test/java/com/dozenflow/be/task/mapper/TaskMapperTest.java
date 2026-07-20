package com.dozenflow.be.task.mapper;

import com.dozenflow.be.task.Task;
import com.dozenflow.be.task.TaskStatus;
import com.dozenflow.be.task.dto.TaskRequestDTO;
import com.dozenflow.be.task.dto.TaskResponseDTO;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TaskMapperTest {

    private final TaskMapper mapper = new TaskMapper();

    @Test
    void toEntity_mapsAllFieldsFromRequestDto() {
        TaskRequestDTO dto = new TaskRequestDTO("Title", "Description", TaskStatus.EM_ANDAMENTO, 2);

        Task task = mapper.toEntity(dto);

        assertThat(task.getId()).isNull();
        assertThat(task.getTitle()).isEqualTo("Title");
        assertThat(task.getDescription()).isEqualTo("Description");
        assertThat(task.getStatus()).isEqualTo(TaskStatus.EM_ANDAMENTO);
        assertThat(task.getTaskOrder()).isEqualTo(2);
    }

    @Test
    void toResponseDTO_mapsAllFieldsFromEntity() {
        Task task = new Task();
        task.setId(10L);
        task.setTitle("Title");
        task.setDescription("Description");
        task.setStatus(TaskStatus.CONCLUIDA);
        task.setTaskOrder(5);

        TaskResponseDTO dto = mapper.toResponseDTO(task);

        assertThat(dto.id()).isEqualTo(10L);
        assertThat(dto.title()).isEqualTo("Title");
        assertThat(dto.description()).isEqualTo("Description");
        assertThat(dto.status()).isEqualTo(TaskStatus.CONCLUIDA);
        assertThat(dto.taskOrder()).isEqualTo(5);
    }
}
