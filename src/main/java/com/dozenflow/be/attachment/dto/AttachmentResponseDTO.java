package com.dozenflow.be.attachment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Data Transfer Object for representing attachment metadata in API responses (never includes the file bytes)")
public record AttachmentResponseDTO(
        @Schema(description = "The unique identifier of the attachment", example = "1")
        Long id,
        @Schema(description = "The original file name", example = "mockup.png")
        String fileName,
        @Schema(description = "The MIME type of the file", example = "image/png")
        String contentType,
        @Schema(description = "The file size in bytes", example = "204800")
        long sizeBytes,
        @Schema(description = "When the attachment was uploaded")
        Instant createdAt
) {}
