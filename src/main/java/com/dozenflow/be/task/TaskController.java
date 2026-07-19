package com.dozenflow.be.task;

import com.dozenflow.be.task.dto.TaskRequestDTO;
import com.dozenflow.be.task.dto.TaskResponseDTO;
import com.dozenflow.be.task.mapper.TaskMapper;
import org.springframework.http.HttpStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Task API", description = "API for managing tasks in the Kanban board")
public class TaskController {

    private final TaskService taskService;
    private final TaskMapper taskMapper;

    public TaskController(TaskService taskService, TaskMapper taskMapper) {
        this.taskService = taskService;
        this.taskMapper = taskMapper;
    }

    @GetMapping
    @Operation(summary = "Get all tasks", description = "Retrieves a list of all tasks, ordered by their position.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    public List<TaskResponseDTO> getAllTasks() {
        return taskService.findAll().stream()
                .map(taskMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @PostMapping
    @Operation(summary = "Create a new task", description = "Creates a new task and returns the created task.")
    @ApiResponse(responseCode = "201", description = "Task created successfully", content = @Content(schema = @Schema(implementation = TaskResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    public ResponseEntity<TaskResponseDTO> createTask(@Valid @RequestBody TaskRequestDTO taskDTO) {
        Task task = taskMapper.toEntity(taskDTO);
        Task createdTask = taskService.create(task);
        return new ResponseEntity<>(taskMapper.toResponseDTO(createdTask), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing task", description = "Updates a task's details by its ID. Can be used to move tasks between columns.")
    @ApiResponse(responseCode = "200", description = "Task updated successfully", content = @Content(schema = @Schema(implementation = TaskResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @ApiResponse(responseCode = "404", description = "Task not found")
    public ResponseEntity<TaskResponseDTO> updateTask(@PathVariable Long id, @Valid @RequestBody TaskRequestDTO taskDTO) {
        Task updatedTask = taskService.update(id, taskDTO);
        return ResponseEntity.ok(taskMapper.toResponseDTO(updatedTask));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a task", description = "Deletes a task by its ID.")
    @ApiResponse(responseCode = "204", description = "Task deleted successfully")
    @ApiResponse(responseCode = "404", description = "Task not found")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/labels/{labelId}")
    @Operation(summary = "Attach a label to a task", description = "Associates an existing label with a task.")
    @ApiResponse(responseCode = "200", description = "Label attached successfully", content = @Content(schema = @Schema(implementation = TaskResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Task or label not found")
    public ResponseEntity<TaskResponseDTO> attachLabel(@PathVariable Long id, @PathVariable Long labelId) {
        Task task = taskService.attachLabel(id, labelId);
        return ResponseEntity.ok(taskMapper.toResponseDTO(task));
    }

    @DeleteMapping("/{id}/labels/{labelId}")
    @Operation(summary = "Detach a label from a task", description = "Removes a label association from a task.")
    @ApiResponse(responseCode = "200", description = "Label detached successfully", content = @Content(schema = @Schema(implementation = TaskResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Task or label not found")
    public ResponseEntity<TaskResponseDTO> detachLabel(@PathVariable Long id, @PathVariable Long labelId) {
        Task task = taskService.detachLabel(id, labelId);
        return ResponseEntity.ok(taskMapper.toResponseDTO(task));
    }
}