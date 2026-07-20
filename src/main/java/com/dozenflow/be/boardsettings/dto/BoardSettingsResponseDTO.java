package com.dozenflow.be.boardsettings.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Data Transfer Object for representing the board's background settings")
public record BoardSettingsResponseDTO(
        @Schema(description = "Id of the selected solid-color background option, or null when a background image is active", example = "ocean")
        String backgroundColorId,
        @Schema(description = "Whether a background image is currently set", example = "false")
        boolean hasBackgroundImage
) {}
