package com.dozenflow.be.comment;

import com.dozenflow.be.comment.dto.CommentRequestDTO;
import com.dozenflow.be.comment.dto.CommentResponseDTO;
import com.dozenflow.be.comment.mapper.CommentMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks/{taskId}/comments")
@Tag(name = "Comment API", description = "API for managing a task's comments")
public class CommentController {

    private final CommentService commentService;
    private final CommentMapper commentMapper;

    public CommentController(CommentService commentService, CommentMapper commentMapper) {
        this.commentService = commentService;
        this.commentMapper = commentMapper;
    }

    @GetMapping
    @Operation(summary = "Get all comments for a task")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    public List<CommentResponseDTO> getAllForTask(@PathVariable Long taskId) {
        return commentService.findAllByTaskId(taskId).stream()
                .map(commentMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @PostMapping
    @Operation(summary = "Add a comment to a task")
    @ApiResponse(responseCode = "201", description = "Comment created successfully", content = @Content(schema = @Schema(implementation = CommentResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @ApiResponse(responseCode = "404", description = "Task not found")
    public ResponseEntity<CommentResponseDTO> create(@PathVariable Long taskId, @Valid @RequestBody CommentRequestDTO dto) {
        Comment comment = commentMapper.toEntity(dto);
        Comment created = commentService.create(taskId, comment);
        return new ResponseEntity<>(commentMapper.toResponseDTO(created), HttpStatus.CREATED);
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "Delete a comment")
    @ApiResponse(responseCode = "204", description = "Comment deleted successfully")
    @ApiResponse(responseCode = "404", description = "Task or comment not found")
    public ResponseEntity<Void> delete(@PathVariable Long taskId, @PathVariable Long commentId) {
        commentService.delete(taskId, commentId);
        return ResponseEntity.noContent().build();
    }
}
