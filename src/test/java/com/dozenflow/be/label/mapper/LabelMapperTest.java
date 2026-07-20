package com.dozenflow.be.label.mapper;

import com.dozenflow.be.label.Label;
import com.dozenflow.be.label.dto.LabelRequestDTO;
import com.dozenflow.be.label.dto.LabelResponseDTO;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LabelMapperTest {

    private final LabelMapper mapper = new LabelMapper();

    @Test
    void toEntity_mapsAllFieldsFromRequestDto() {
        LabelRequestDTO dto = new LabelRequestDTO("Urgente", "#eb5a46");

        Label label = mapper.toEntity(dto);

        assertThat(label.getId()).isNull();
        assertThat(label.getName()).isEqualTo("Urgente");
        assertThat(label.getColorHex()).isEqualTo("#eb5a46");
    }

    @Test
    void toResponseDTO_mapsAllFieldsFromEntity() {
        Label label = new Label();
        label.setId(1L);
        label.setName("Urgente");
        label.setColorHex("#eb5a46");

        LabelResponseDTO dto = mapper.toResponseDTO(label);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.name()).isEqualTo("Urgente");
        assertThat(dto.colorHex()).isEqualTo("#eb5a46");
    }
}
