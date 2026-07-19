package com.dozenflow.be.label.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Data Transfer Object for creating or updating a label")
public record LabelRequestDTO(
        @Schema(description = "Optional display name for the label", example = "Urgente")
        String name,
        @NotBlank(message = "Color cannot be blank")
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be a hex code like #61bd4f")
        @Schema(description = "Hex color code for the label", example = "#61bd4f")
        String colorHex
) {}
