package com.dozenflow.be.boardsettings.mapper;

import com.dozenflow.be.boardsettings.BoardSettings;
import com.dozenflow.be.boardsettings.dto.BoardSettingsResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class BoardSettingsMapper {

    public BoardSettingsResponseDTO toResponseDTO(BoardSettings entity) {
        return new BoardSettingsResponseDTO(entity.getBackgroundColorId(), entity.getBackgroundImage() != null);
    }
}
