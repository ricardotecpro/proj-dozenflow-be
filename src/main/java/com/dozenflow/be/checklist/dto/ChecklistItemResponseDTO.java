package com.dozenflow.be.checklist.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Data Transfer Object for representing a checklist item in API responses")
public record ChecklistItemResponseDTO(
        @Schema(description = "The unique identifier of the checklist item", example = "1")
        Long id,
        @Schema(description = "The title of the checklist item", example = "Escrever testes")
        String title,
        @Schema(description = "Whether the item is checked off", example = "false")
        boolean done,
        @Schema(description = "The vertical order of the item within the checklist", example = "0")
        int itemOrder
) {}
