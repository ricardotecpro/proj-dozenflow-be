package com.dozenflow.be.task.dto;

import com.dozenflow.be.label.dto.LabelResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Data Transfer Object for representing a task in API responses")
public record TaskResponseDTO(
        @Schema(description = "The unique identifier of the task", example = "1")
        Long id,
        @Schema(description = "The title of the task", example = "Implement new feature")
        String title,
        @Schema(description = "A detailed description of the task", example = "Implement the new user authentication feature using JWT.")
        String description,
        @Schema(description = "The id of the list (column) this task belongs to", example = "1")
        Long listId,
        @Schema(description = "The vertical order of the task within its list", example = "1")
        int taskOrder,
        @Schema(description = "Whether the task is archived", example = "false")
        boolean archived,
        @Schema(description = "Optional due date for the task", example = "2026-08-01")
        LocalDate dueDate,
        @Schema(description = "Optional cover color for the card, as a hex string", example = "#0079bf")
        String coverColor,
        @Schema(description = "Optional cover size for the card (\"HEADER\" or \"FULL\")", example = "HEADER")
        String coverSize,
        @Schema(description = "Optional id of an attachment of this task used as its cover image, instead of coverColor", example = "1")
        Long coverAttachmentId,
        @Schema(description = "Labels attached to this task")
        List<LabelResponseDTO> labels,
        @Schema(description = "Total number of checklist items", example = "5")
        int checklistTotal,
        @Schema(description = "Number of checked-off checklist items", example = "2")
        int checklistDone,
        @Schema(description = "Total number of comments on this task", example = "3")
        int commentCount,
        @Schema(description = "Total number of file attachments on this task", example = "1")
        int attachmentCount
) {}