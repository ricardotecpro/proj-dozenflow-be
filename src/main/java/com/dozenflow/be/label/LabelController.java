package com.dozenflow.be.label;

import com.dozenflow.be.label.dto.LabelRequestDTO;
import com.dozenflow.be.label.dto.LabelResponseDTO;
import com.dozenflow.be.label.mapper.LabelMapper;
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
@RequestMapping("/api/labels")
@Tag(name = "Label API", description = "API for managing the board-wide label catalog")
public class LabelController {

    private final LabelService labelService;
    private final LabelMapper labelMapper;

    public LabelController(LabelService labelService, LabelMapper labelMapper) {
        this.labelService = labelService;
        this.labelMapper = labelMapper;
    }

    @GetMapping
    @Operation(summary = "Get all labels", description = "Retrieves the board-wide label catalog.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    public List<LabelResponseDTO> getAllLabels() {
        return labelService.findAll().stream()
                .map(labelMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @PostMapping
    @Operation(summary = "Create a new label", description = "Adds a new label to the board-wide catalog.")
    @ApiResponse(responseCode = "201", description = "Label created successfully", content = @Content(schema = @Schema(implementation = LabelResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    public ResponseEntity<LabelResponseDTO> createLabel(@Valid @RequestBody LabelRequestDTO dto) {
        Label label = labelMapper.toEntity(dto);
        Label created = labelService.create(label);
        return new ResponseEntity<>(labelMapper.toResponseDTO(created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a label", description = "Updates a label's name and/or color by its ID.")
    @ApiResponse(responseCode = "200", description = "Label updated successfully", content = @Content(schema = @Schema(implementation = LabelResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @ApiResponse(responseCode = "404", description = "Label not found")
    public ResponseEntity<LabelResponseDTO> updateLabel(@PathVariable Long id, @Valid @RequestBody LabelRequestDTO dto) {
        Label label = labelMapper.toEntity(dto);
        Label updated = labelService.update(id, label);
        return ResponseEntity.ok(labelMapper.toResponseDTO(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a label", description = "Deletes a label by its ID, detaching it from any tasks.")
    @ApiResponse(responseCode = "204", description = "Label deleted successfully")
    @ApiResponse(responseCode = "404", description = "Label not found")
    public ResponseEntity<Void> deleteLabel(@PathVariable Long id) {
        labelService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
