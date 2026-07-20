package com.dozenflow.be.list;

import com.dozenflow.be.list.dto.TaskListRequestDTO;
import com.dozenflow.be.list.dto.TaskListResponseDTO;
import com.dozenflow.be.list.mapper.TaskListMapper;
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
@RequestMapping("/api/lists")
@Tag(name = "List API", description = "API for managing the Kanban board's lists (columns)")
public class TaskListController {

    private final TaskListService taskListService;
    private final TaskListMapper taskListMapper;

    public TaskListController(TaskListService taskListService, TaskListMapper taskListMapper) {
        this.taskListService = taskListService;
        this.taskListMapper = taskListMapper;
    }

    @GetMapping
    @Operation(summary = "Get all active lists", description = "Retrieves all non-archived lists, ordered by position.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    public List<TaskListResponseDTO> getAllLists() {
        return taskListService.findAll().stream()
                .map(taskListMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/archived")
    @Operation(summary = "Get all archived lists", description = "Retrieves all archived lists, ordered by position.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    public List<TaskListResponseDTO> getArchivedLists() {
        return taskListService.findArchived().stream()
                .map(taskListMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @PostMapping
    @Operation(summary = "Create a new list", description = "Adds a new list (column) to the board.")
    @ApiResponse(responseCode = "201", description = "List created successfully", content = @Content(schema = @Schema(implementation = TaskListResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    public ResponseEntity<TaskListResponseDTO> createList(@Valid @RequestBody TaskListRequestDTO dto) {
        TaskList list = taskListMapper.toEntity(dto);
        TaskList created = taskListService.create(list);
        return new ResponseEntity<>(taskListMapper.toResponseDTO(created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a list", description = "Updates a list's name and/or position by its ID.")
    @ApiResponse(responseCode = "200", description = "List updated successfully", content = @Content(schema = @Schema(implementation = TaskListResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @ApiResponse(responseCode = "404", description = "List not found")
    public ResponseEntity<TaskListResponseDTO> updateList(@PathVariable Long id, @Valid @RequestBody TaskListRequestDTO dto) {
        TaskList list = taskListMapper.toEntity(dto);
        TaskList updated = taskListService.update(id, list);
        return ResponseEntity.ok(taskListMapper.toResponseDTO(updated));
    }

    @PostMapping("/{id}/archive")
    @Operation(summary = "Archive a list", description = "Archives a list and cascades the archive to all of its tasks.")
    @ApiResponse(responseCode = "200", description = "List archived successfully", content = @Content(schema = @Schema(implementation = TaskListResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Cannot archive the last remaining list")
    @ApiResponse(responseCode = "404", description = "List not found")
    public ResponseEntity<TaskListResponseDTO> archiveList(@PathVariable Long id) {
        TaskList archived = taskListService.archive(id);
        return ResponseEntity.ok(taskListMapper.toResponseDTO(archived));
    }

    @PostMapping("/{id}/restore")
    @Operation(summary = "Restore a list", description = "Restores an archived list. Its tasks stay archived.")
    @ApiResponse(responseCode = "200", description = "List restored successfully", content = @Content(schema = @Schema(implementation = TaskListResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "List not found")
    public ResponseEntity<TaskListResponseDTO> restoreList(@PathVariable Long id) {
        TaskList restored = taskListService.restore(id);
        return ResponseEntity.ok(taskListMapper.toResponseDTO(restored));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Permanently delete a list", description = "Permanently deletes a list and its tasks. Intended for use from the archive panel.")
    @ApiResponse(responseCode = "204", description = "List deleted successfully")
    @ApiResponse(responseCode = "400", description = "Cannot delete the last remaining list")
    @ApiResponse(responseCode = "404", description = "List not found")
    public ResponseEntity<Void> deleteList(@PathVariable Long id) {
        taskListService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
