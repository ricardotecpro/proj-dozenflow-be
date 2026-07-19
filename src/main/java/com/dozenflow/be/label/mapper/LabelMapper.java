package com.dozenflow.be.label.mapper;

import com.dozenflow.be.label.Label;
import com.dozenflow.be.label.dto.LabelRequestDTO;
import com.dozenflow.be.label.dto.LabelResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class LabelMapper {

    public Label toEntity(LabelRequestDTO dto) {
        Label label = new Label();
        label.setName(dto.name());
        label.setColorHex(dto.colorHex());
        return label;
    }

    public LabelResponseDTO toResponseDTO(Label entity) {
        return new LabelResponseDTO(entity.getId(), entity.getName(), entity.getColorHex());
    }
}
