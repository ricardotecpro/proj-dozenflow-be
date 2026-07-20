package com.dozenflow.be.attachment.mapper;

import com.dozenflow.be.attachment.Attachment;
import com.dozenflow.be.attachment.AttachmentSummary;
import com.dozenflow.be.attachment.dto.AttachmentResponseDTO;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AttachmentMapperTest {

    private final AttachmentMapper mapper = new AttachmentMapper();

    @Test
    void toResponseDTO_mapsAllFieldsFromEntity_excludingData() {
        Instant now = Instant.now();
        Attachment attachment = new Attachment();
        attachment.setId(1L);
        attachment.setFileName("mockup.png");
        attachment.setContentType("image/png");
        attachment.setSizeBytes(1024);
        attachment.setData(new byte[] {1, 2, 3});
        attachment.setCreatedAt(now);

        AttachmentResponseDTO dto = mapper.toResponseDTO(attachment);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.fileName()).isEqualTo("mockup.png");
        assertThat(dto.contentType()).isEqualTo("image/png");
        assertThat(dto.sizeBytes()).isEqualTo(1024);
        assertThat(dto.createdAt()).isEqualTo(now);
    }

    @Test
    void toResponseDTO_mapsAllFieldsFromSummaryProjection() {
        Instant now = Instant.now();
        AttachmentSummary summary = mock(AttachmentSummary.class);
        when(summary.getId()).thenReturn(1L);
        when(summary.getFileName()).thenReturn("mockup.png");
        when(summary.getContentType()).thenReturn("image/png");
        when(summary.getSizeBytes()).thenReturn(1024L);
        when(summary.getCreatedAt()).thenReturn(now);

        AttachmentResponseDTO dto = mapper.toResponseDTO(summary);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.fileName()).isEqualTo("mockup.png");
        assertThat(dto.sizeBytes()).isEqualTo(1024);
        assertThat(dto.createdAt()).isEqualTo(now);
    }
}
