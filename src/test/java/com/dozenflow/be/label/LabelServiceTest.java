package com.dozenflow.be.label;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LabelServiceTest {

    @Mock
    private LabelRepository labelRepository;

    private LabelService labelService;

    @BeforeEach
    void setUp() {
        labelService = new LabelService(labelRepository);
    }

    @Test
    void findAll_returnsAllLabels() {
        Label label = new Label();
        label.setId(1L);
        when(labelRepository.findAll()).thenReturn(List.of(label));

        List<Label> result = labelService.findAll();

        assertThat(result).containsExactly(label);
    }

    @Test
    void create_savesAndReturnsLabel() {
        Label label = new Label();
        label.setColorHex("#61bd4f");
        when(labelRepository.save(label)).thenReturn(label);

        Label result = labelService.create(label);

        assertThat(result).isEqualTo(label);
    }

    @Test
    void update_appliesFieldsAndSaves_whenLabelExists() {
        Label existing = new Label();
        existing.setId(1L);
        existing.setName("Old");
        existing.setColorHex("#61bd4f");

        Label updated = new Label();
        updated.setName("New");
        updated.setColorHex("#eb5a46");

        when(labelRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(labelRepository.save(any(Label.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Label result = labelService.update(1L, updated);

        assertThat(result.getName()).isEqualTo("New");
        assertThat(result.getColorHex()).isEqualTo("#eb5a46");
    }

    @Test
    void update_throwsEntityNotFoundException_whenLabelDoesNotExist() {
        when(labelRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> labelService.update(99L, new Label()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void delete_removesLabel_whenLabelExists() {
        when(labelRepository.existsById(1L)).thenReturn(true);

        labelService.delete(1L);

        verify(labelRepository).deleteById(1L);
    }

    @Test
    void delete_throwsEntityNotFoundException_whenLabelDoesNotExist() {
        when(labelRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> labelService.delete(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");

        verify(labelRepository, never()).deleteById(any());
    }
}
