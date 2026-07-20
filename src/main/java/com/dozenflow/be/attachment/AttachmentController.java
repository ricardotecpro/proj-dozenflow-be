package com.dozenflow.be.attachment;

import com.dozenflow.be.attachment.dto.AttachmentResponseDTO;
import com.dozenflow.be.attachment.mapper.AttachmentMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks/{taskId}/attachments")
@Tag(name = "Attachment API", description = "API for managing a task's file attachments")
public class AttachmentController {

    private final AttachmentService attachmentService;
    private final AttachmentMapper attachmentMapper;

    public AttachmentController(AttachmentService attachmentService, AttachmentMapper attachmentMapper) {
        this.attachmentService = attachmentService;
        this.attachmentMapper = attachmentMapper;
    }

    @GetMapping
    @Operation(summary = "Get attachment metadata for a task", description = "Lists attachments without their file bytes.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    public List<AttachmentResponseDTO> getAllForTask(@PathVariable Long taskId) {
        return attachmentService.findAllByTaskId(taskId).stream()
                .map(attachmentMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload an attachment", description = "Max 5MB; allowlisted content types only.")
    @ApiResponse(responseCode = "201", description = "Attachment uploaded successfully", content = @Content(schema = @Schema(implementation = AttachmentResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid file (empty, too large, or unsupported type)")
    @ApiResponse(responseCode = "404", description = "Task not found")
    public ResponseEntity<AttachmentResponseDTO> upload(@PathVariable Long taskId, @RequestParam("file") MultipartFile file) {
        Attachment created = attachmentService.upload(taskId, file);
        return new ResponseEntity<>(attachmentMapper.toResponseDTO(created), HttpStatus.CREATED);
    }

    @GetMapping("/{attachmentId}/download")
    @Operation(summary = "Download an attachment's file bytes")
    @ApiResponse(responseCode = "200", description = "File stream")
    @ApiResponse(responseCode = "404", description = "Task or attachment not found")
    public ResponseEntity<byte[]> download(@PathVariable Long taskId, @PathVariable Long attachmentId) {
        Attachment attachment = attachmentService.getForDownload(taskId, attachmentId);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(attachment.getFileName(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(attachment.getData());
    }

    @GetMapping("/{attachmentId}/view")
    @Operation(summary = "View an attachment's file bytes inline", description = "Same bytes as /download, but served inline (not forced as a file save) and cacheable, so it can be used directly as an <img src> or CSS background-image — e.g. for card covers.")
    @ApiResponse(responseCode = "200", description = "File stream")
    @ApiResponse(responseCode = "404", description = "Task or attachment not found")
    public ResponseEntity<byte[]> view(@PathVariable Long taskId, @PathVariable Long attachmentId) {
        Attachment attachment = attachmentService.getForDownload(taskId, attachmentId);
        ContentDisposition disposition = ContentDisposition.inline()
                .filename(attachment.getFileName(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .header(HttpHeaders.CACHE_CONTROL, "private, max-age=3600")
                .body(attachment.getData());
    }

    @DeleteMapping("/{attachmentId}")
    @Operation(summary = "Delete an attachment")
    @ApiResponse(responseCode = "204", description = "Attachment deleted successfully")
    @ApiResponse(responseCode = "404", description = "Task or attachment not found")
    public ResponseEntity<Void> delete(@PathVariable Long taskId, @PathVariable Long attachmentId) {
        attachmentService.delete(taskId, attachmentId);
        return ResponseEntity.noContent().build();
    }
}
