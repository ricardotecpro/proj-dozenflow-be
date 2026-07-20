package com.dozenflow.be.boardsettings.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Data Transfer Object for updating the board's solid-color background")
public record BoardSettingsUpdateDTO(
        @NotBlank(message = "backgroundColorId cannot be blank")
        @Schema(description = "Id of the solid-color background option to select", example = "ocean")
        String backgroundColorId
) {}
