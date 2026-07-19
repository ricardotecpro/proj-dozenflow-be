package com.dozenflow.be.checklist.mapper;

import com.dozenflow.be.checklist.ChecklistItem;
import com.dozenflow.be.checklist.dto.ChecklistItemRequestDTO;
import com.dozenflow.be.checklist.dto.ChecklistItemResponseDTO;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChecklistItemMapperTest {

    private final ChecklistItemMapper mapper = new ChecklistItemMapper();

    @Test
    void toEntity_mapsAllFieldsFromRequestDto() {
        ChecklistItemRequestDTO dto = new ChecklistItemRequestDTO("Title", true, 2);

        ChecklistItem item = mapper.toEntity(dto);

        assertThat(item.getId()).isNull();
        assertThat(item.getTitle()).isEqualTo("Title");
        assertThat(item.isDone()).isTrue();
        assertThat(item.getItemOrder()).isEqualTo(2);
    }

    @Test
    void toEntity_treatsNullDoneAsFalse() {
        ChecklistItemRequestDTO dto = new ChecklistItemRequestDTO("Title", null, 0);

        ChecklistItem item = mapper.toEntity(dto);

        assertThat(item.isDone()).isFalse();
    }

    @Test
    void toResponseDTO_mapsAllFieldsFromEntity() {
        ChecklistItem item = new ChecklistItem();
        item.setId(1L);
        item.setTitle("Title");
        item.setDone(true);
        item.setItemOrder(3);

        ChecklistItemResponseDTO dto = mapper.toResponseDTO(item);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.title()).isEqualTo("Title");
        assertThat(dto.done()).isTrue();
        assertThat(dto.itemOrder()).isEqualTo(3);
    }
}
