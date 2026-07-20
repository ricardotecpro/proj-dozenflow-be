package com.dozenflow.be.list.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Data Transfer Object for representing a list in API responses")
public record TaskListResponseDTO(
        @Schema(description = "The unique identifier of the list", example = "1")
        Long id,
        @Schema(description = "Display name for the list", example = "Backlog")
        String name,
        @Schema(description = "The horizontal order of the list on the board", example = "0")
        int position,
        @Schema(description = "Whether the list is archived", example = "false")
        boolean archived
) {}
