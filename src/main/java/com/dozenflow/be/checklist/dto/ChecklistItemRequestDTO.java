package com.dozenflow.be.checklist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Data Transfer Object for creating or updating a checklist item")
public record ChecklistItemRequestDTO(
        @NotBlank(message = "Title cannot be blank")
        @Schema(description = "The title of the checklist item", example = "Escrever testes")
        String title,
        @NotNull(message = "Done cannot be null")
        @Schema(description = "Whether the item is checked off", example = "false")
        Boolean done,
        @NotNull(message = "Item order cannot be null")
        @Schema(description = "The vertical order of the item within the checklist", example = "0")
        Integer itemOrder
) {}
