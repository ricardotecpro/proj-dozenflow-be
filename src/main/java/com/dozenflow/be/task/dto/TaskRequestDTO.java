package com.dozenflow.be.task.dto;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(description = "Data Transfer Object for creating or updating a task")
public record TaskRequestDTO(
        @NotBlank(message = "Title cannot be blank")
        @Schema(description = "The title of the task", example = "Implement new feature")
        String title,
        @Schema(description = "A detailed description of the task", example = "Implement the new user authentication feature using JWT.")
        String description,
        @NotNull(message = "List cannot be null")
        @Schema(description = "The id of the list (column) this task belongs to", example = "1")
        Long listId,
        @NotNull(message = "Task order cannot be null")
        @Schema(description = "The vertical order of the task within its list", example = "1")
        int taskOrder,
        @Schema(description = "Optional due date for the task", example = "2026-08-01")
        LocalDate dueDate,
        @Schema(description = "Optional cover color for the card, as a hex string", example = "#0079bf")
        String coverColor,
        @Schema(description = "Optional cover size for the card (\"HEADER\" or \"FULL\")", example = "HEADER")
        String coverSize,
        @Schema(description = "Optional id of an attachment of this task to use as its cover image, instead of coverColor", example = "1")
        Long coverAttachmentId
) {}