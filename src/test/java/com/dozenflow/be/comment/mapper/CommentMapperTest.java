package com.dozenflow.be.comment.mapper;

import com.dozenflow.be.comment.Comment;
import com.dozenflow.be.comment.dto.CommentRequestDTO;
import com.dozenflow.be.comment.dto.CommentResponseDTO;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class CommentMapperTest {

    private final CommentMapper mapper = new CommentMapper();

    @Test
    void toEntity_mapsBodyFromRequestDto() {
        CommentRequestDTO dto = new CommentRequestDTO("Nice work");

        Comment comment = mapper.toEntity(dto);

        assertThat(comment.getId()).isNull();
        assertThat(comment.getBody()).isEqualTo("Nice work");
    }

    @Test
    void toResponseDTO_mapsAllFieldsFromEntity() {
        Instant now = Instant.now();
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setBody("Nice work");
        comment.setCreatedAt(now);

        CommentResponseDTO dto = mapper.toResponseDTO(comment);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.body()).isEqualTo("Nice work");
        assertThat(dto.createdAt()).isEqualTo(now);
    }
}
