package com.dozenflow.be.comment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Data Transfer Object for creating a comment")
public record CommentRequestDTO(
        @NotBlank(message = "Body cannot be blank")
        @Size(max = 2000, message = "Body must be at most 2000 characters")
        @Schema(description = "The comment text", example = "Já revisei, pode seguir.")
        String body
) {}
