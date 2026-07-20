package com.dozenflow.be.checklist;

import com.dozenflow.be.checklist.dto.ChecklistItemRequestDTO;
import com.dozenflow.be.checklist.dto.ChecklistItemResponseDTO;
import com.dozenflow.be.checklist.mapper.ChecklistItemMapper;
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
@RequestMapping("/api/tasks/{taskId}/checklist-items")
@Tag(name = "Checklist API", description = "API for managing a task's checklist items")
public class ChecklistItemController {

    private final ChecklistItemService checklistItemService;
    private final ChecklistItemMapper checklistItemMapper;

    public ChecklistItemController(ChecklistItemService checklistItemService, ChecklistItemMapper checklistItemMapper) {
        this.checklistItemService = checklistItemService;
        this.checklistItemMapper = checklistItemMapper;
    }

    @GetMapping
    @Operation(summary = "Get all checklist items for a task")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    public List<ChecklistItemResponseDTO> getAllForTask(@PathVariable Long taskId) {
        return checklistItemService.findAllByTaskId(taskId).stream()
                .map(checklistItemMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @PostMapping
    @Operation(summary = "Create a checklist item on a task")
    @ApiResponse(responseCode = "201", description = "Item created successfully", content = @Content(schema = @Schema(implementation = ChecklistItemResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @ApiResponse(responseCode = "404", description = "Task not found")
    public ResponseEntity<ChecklistItemResponseDTO> create(@PathVariable Long taskId, @Valid @RequestBody ChecklistItemRequestDTO dto) {
        ChecklistItem item = checklistItemMapper.toEntity(dto);
        ChecklistItem created = checklistItemService.create(taskId, item);
        return new ResponseEntity<>(checklistItemMapper.toResponseDTO(created), HttpStatus.CREATED);
    }

    @PutMapping("/{itemId}")
    @Operation(summary = "Update a checklist item")
    @ApiResponse(responseCode = "200", description = "Item updated successfully", content = @Content(schema = @Schema(implementation = ChecklistItemResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @ApiResponse(responseCode = "404", description = "Task or item not found")
    public ResponseEntity<ChecklistItemResponseDTO> update(
            @PathVariable Long taskId, @PathVariable Long itemId, @Valid @RequestBody ChecklistItemRequestDTO dto) {
        ChecklistItem item = checklistItemMapper.toEntity(dto);
        ChecklistItem updated = checklistItemService.update(taskId, itemId, item);
        return ResponseEntity.ok(checklistItemMapper.toResponseDTO(updated));
    }

    @DeleteMapping("/{itemId}")
    @Operation(summary = "Delete a checklist item")
    @ApiResponse(responseCode = "204", description = "Item deleted successfully")
    @ApiResponse(responseCode = "404", description = "Task or item not found")
    public ResponseEntity<Void> delete(@PathVariable Long taskId, @PathVariable Long itemId) {
        checklistItemService.delete(taskId, itemId);
        return ResponseEntity.noContent().build();
    }
}
