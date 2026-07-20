package com.dozenflow.be.list.mapper;

import com.dozenflow.be.list.TaskList;
import com.dozenflow.be.list.dto.TaskListRequestDTO;
import com.dozenflow.be.list.dto.TaskListResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class TaskListMapper {

    public TaskList toEntity(TaskListRequestDTO dto) {
        TaskList list = new TaskList();
        list.setName(dto.name());
        list.setPosition(dto.position());
        return list;
    }

    public TaskListResponseDTO toResponseDTO(TaskList entity) {
        return new TaskListResponseDTO(entity.getId(), entity.getName(), entity.getPosition(), entity.isArchived());
    }
}
