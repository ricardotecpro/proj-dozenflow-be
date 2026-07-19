package com.dozenflow.be.comment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Data Transfer Object for representing a comment in API responses")
public record CommentResponseDTO(
        @Schema(description = "The unique identifier of the comment", example = "1")
        Long id,
        @Schema(description = "The comment text", example = "Já revisei, pode seguir.")
        String body,
        @Schema(description = "When the comment was created")
        Instant createdAt
) {}
