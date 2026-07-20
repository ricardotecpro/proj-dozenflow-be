package com.dozenflow.be.list.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Data Transfer Object for creating or updating a list")
public record TaskListRequestDTO(
        @NotBlank(message = "Name cannot be blank")
        @Schema(description = "Display name for the list", example = "Backlog")
        String name,
        @NotNull(message = "Position cannot be null")
        @Schema(description = "The horizontal order of the list on the board", example = "0")
        Integer position
) {}
