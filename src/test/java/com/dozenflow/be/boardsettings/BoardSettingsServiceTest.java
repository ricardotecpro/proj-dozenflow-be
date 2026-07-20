package com.dozenflow.be.boardsettings;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoardSettingsServiceTest {

    @Mock
    private BoardSettingsRepository boardSettingsRepository;

    private BoardSettingsService boardSettingsService;

    @BeforeEach
    void setUp() {
        boardSettingsService = new BoardSettingsService(boardSettingsRepository);
    }

    @Test
    void get_returnsAFreshDefaultRow_whenNoneExistsYet() {
        when(boardSettingsRepository.findById(1L)).thenReturn(Optional.empty());

        BoardSettings result = boardSettingsService.get();

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getBackgroundColorId()).isNull();
        assertThat(result.getBackgroundImage()).isNull();
    }

    @Test
    void updateColor_setsColorAndClearsImage() {
        BoardSettings existing = new BoardSettings();
        existing.setId(1L);
        existing.setBackgroundImage("fake-bytes".getBytes());
        existing.setBackgroundImageContentType("image/png");

        when(boardSettingsRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(boardSettingsRepository.save(any(BoardSettings.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BoardSettings result = boardSettingsService.updateColor("ocean");

        assertThat(result.getBackgroundColorId()).isEqualTo("ocean");
        assertThat(result.getBackgroundImage()).isNull();
        assertThat(result.getBackgroundImageContentType()).isNull();
    }

    @Test
    void uploadImage_setsImageAndClearsColor_whenFileIsValid() {
        BoardSettings existing = new BoardSettings();
        existing.setId(1L);
        existing.setBackgroundColorId("ocean");
        MockMultipartFile file = new MockMultipartFile("file", "beach.png", "image/png", "fake-bytes".getBytes());

        when(boardSettingsRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(boardSettingsRepository.save(any(BoardSettings.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BoardSettings result = boardSettingsService.uploadImage(file);

        assertThat(result.getBackgroundImage()).isEqualTo("fake-bytes".getBytes());
        assertThat(result.getBackgroundImageContentType()).isEqualTo("image/png");
        assertThat(result.getBackgroundColorId()).isNull();
    }

    @Test
    void uploadImage_throwsInvalidBackgroundImageException_whenFileIsEmpty() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.png", "image/png", new byte[0]);

        assertThatThrownBy(() -> boardSettingsService.uploadImage(file))
                .isInstanceOf(InvalidBackgroundImageException.class);

        verify(boardSettingsRepository, never()).save(any());
    }

    @Test
    void uploadImage_throwsInvalidBackgroundImageException_whenFileExceedsSizeLimit() {
        byte[] tooLarge = new byte[(int) BoardSettingsService.MAX_IMAGE_SIZE_BYTES + 1];
        MockMultipartFile file = new MockMultipartFile("file", "big.png", "image/png", tooLarge);

        assertThatThrownBy(() -> boardSettingsService.uploadImage(file))
                .isInstanceOf(InvalidBackgroundImageException.class)
                .hasMessageContaining("5MB");

        verify(boardSettingsRepository, never()).save(any());
    }

    @Test
    void uploadImage_throwsInvalidBackgroundImageException_whenContentTypeIsNotAnImage() {
        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", "fake-bytes".getBytes());

        assertThatThrownBy(() -> boardSettingsService.uploadImage(file))
                .isInstanceOf(InvalidBackgroundImageException.class)
                .hasMessageContaining("Unsupported");

        verify(boardSettingsRepository, never()).save(any());
    }

    @Test
    void removeImage_clearsImageFields() {
        BoardSettings existing = new BoardSettings();
        existing.setId(1L);
        existing.setBackgroundImage("fake-bytes".getBytes());
        existing.setBackgroundImageContentType("image/png");

        when(boardSettingsRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(boardSettingsRepository.save(any(BoardSettings.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BoardSettings result = boardSettingsService.removeImage();

        assertThat(result.getBackgroundImage()).isNull();
        assertThat(result.getBackgroundImageContentType()).isNull();
    }

    @Test
    void getImage_returnsSettings_whenImageIsSet() {
        BoardSettings existing = new BoardSettings();
        existing.setId(1L);
        existing.setBackgroundImage("fake-bytes".getBytes());
        existing.setBackgroundImageContentType("image/png");

        when(boardSettingsRepository.findById(1L)).thenReturn(Optional.of(existing));

        BoardSettings result = boardSettingsService.getImage();

        assertThat(result.getBackgroundImage()).isEqualTo("fake-bytes".getBytes());
    }

    @Test
    void getImage_throwsEntityNotFoundException_whenNoImageIsSet() {
        BoardSettings existing = new BoardSettings();
        existing.setId(1L);
        existing.setBackgroundColorId("ocean");

        when(boardSettingsRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> boardSettingsService.getImage())
                .isInstanceOf(EntityNotFoundException.class);
    }
}
