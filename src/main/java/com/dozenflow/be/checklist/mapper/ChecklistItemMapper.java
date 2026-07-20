package com.dozenflow.be.checklist.mapper;

import com.dozenflow.be.checklist.ChecklistItem;
import com.dozenflow.be.checklist.dto.ChecklistItemRequestDTO;
import com.dozenflow.be.checklist.dto.ChecklistItemResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class ChecklistItemMapper {

    public ChecklistItem toEntity(ChecklistItemRequestDTO dto) {
        ChecklistItem item = new ChecklistItem();
        item.setTitle(dto.title());
        item.setDone(Boolean.TRUE.equals(dto.done()));
        item.setItemOrder(dto.itemOrder());
        return item;
    }

    public ChecklistItemResponseDTO toResponseDTO(ChecklistItem entity) {
        return new ChecklistItemResponseDTO(entity.getId(), entity.getTitle(), entity.isDone(), entity.getItemOrder());
    }
}
