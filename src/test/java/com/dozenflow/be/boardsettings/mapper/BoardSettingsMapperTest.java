package com.dozenflow.be.boardsettings.mapper;

import com.dozenflow.be.boardsettings.BoardSettings;
import com.dozenflow.be.boardsettings.dto.BoardSettingsResponseDTO;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BoardSettingsMapperTest {

    private final BoardSettingsMapper mapper = new BoardSettingsMapper();

    @Test
    void toResponseDTO_mapsColorAndNoImage() {
        BoardSettings settings = new BoardSettings();
        settings.setId(1L);
        settings.setBackgroundColorId("ocean");

        BoardSettingsResponseDTO dto = mapper.toResponseDTO(settings);

        assertThat(dto.backgroundColorId()).isEqualTo("ocean");
        assertThat(dto.hasBackgroundImage()).isFalse();
    }

    @Test
    void toResponseDTO_mapsHasBackgroundImageTrue_whenImageIsSet() {
        BoardSettings settings = new BoardSettings();
        settings.setId(1L);
        settings.setBackgroundImage("fake-bytes".getBytes());
        settings.setBackgroundImageContentType("image/png");

        BoardSettingsResponseDTO dto = mapper.toResponseDTO(settings);

        assertThat(dto.backgroundColorId()).isNull();
        assertThat(dto.hasBackgroundImage()).isTrue();
    }
}
