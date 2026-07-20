package com.dozenflow.be.label.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Data Transfer Object for representing a label in API responses")
public record LabelResponseDTO(
        @Schema(description = "The unique identifier of the label", example = "1")
        Long id,
        @Schema(description = "Optional display name for the label", example = "Urgente")
        String name,
        @Schema(description = "Hex color code for the label", example = "#61bd4f")
        String colorHex
) {}
