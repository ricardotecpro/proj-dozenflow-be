package com.dozenflow.be.task.dto;

import com.dozenflow.be.task.TaskStatus;
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
        @NotNull(message = "Status cannot be null")
        @Schema(description = "The current status of the task", example = "A_FAZER")
        TaskStatus status,
        @NotNull(message = "Task order cannot be null")
        @Schema(description = "The vertical order of the task within its status column", example = "1")
        int taskOrder,
        @Schema(description = "Optional due date for the task", example = "2026-08-01")
        LocalDate dueDate
) {}