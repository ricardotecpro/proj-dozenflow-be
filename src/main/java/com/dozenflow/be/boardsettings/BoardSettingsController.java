package com.dozenflow.be.boardsettings;

import com.dozenflow.be.boardsettings.dto.BoardSettingsResponseDTO;
import com.dozenflow.be.boardsettings.dto.BoardSettingsUpdateDTO;
import com.dozenflow.be.boardsettings.mapper.BoardSettingsMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/board-settings")
@Tag(name = "Board Settings API", description = "API for managing the board's background (solid color or image)")
public class BoardSettingsController {

    private final BoardSettingsService boardSettingsService;
    private final BoardSettingsMapper boardSettingsMapper;

    public BoardSettingsController(BoardSettingsService boardSettingsService, BoardSettingsMapper boardSettingsMapper) {
        this.boardSettingsService = boardSettingsService;
        this.boardSettingsMapper = boardSettingsMapper;
    }

    @GetMapping
    @Operation(summary = "Get the board's current background settings")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved settings")
    public BoardSettingsResponseDTO get() {
        return boardSettingsMapper.toResponseDTO(boardSettingsService.get());
    }

    @PutMapping
    @Operation(summary = "Select a solid-color background", description = "Clears any background image previously set.")
    @ApiResponse(responseCode = "200", description = "Settings updated successfully")
    public BoardSettingsResponseDTO updateColor(@Valid @RequestBody BoardSettingsUpdateDTO dto) {
        return boardSettingsMapper.toResponseDTO(boardSettingsService.updateColor(dto.backgroundColorId()));
    }

    @PostMapping(path = "/background-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a background image", description = "Max 5MB; allowlisted image content types only. Clears any solid-color selection.")
    @ApiResponse(responseCode = "201", description = "Background image uploaded successfully", content = @Content(schema = @Schema(implementation = BoardSettingsResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid file (empty, too large, or unsupported type)")
    public ResponseEntity<BoardSettingsResponseDTO> uploadImage(@RequestParam("file") MultipartFile file) {
        BoardSettings settings = boardSettingsService.uploadImage(file);
        return new ResponseEntity<>(boardSettingsMapper.toResponseDTO(settings), org.springframework.http.HttpStatus.CREATED);
    }

    @GetMapping("/background-image")
    @Operation(summary = "View the current background image inline", description = "Cacheable, meant for direct use as a CSS background-image URL.")
    @ApiResponse(responseCode = "200", description = "Image stream")
    @ApiResponse(responseCode = "404", description = "No background image is currently set")
    public ResponseEntity<byte[]> getImage() {
        BoardSettings settings = boardSettingsService.getImage();
        ContentDisposition disposition = ContentDisposition.inline()
                .filename("board-background", StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(settings.getBackgroundImageContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .header(HttpHeaders.CACHE_CONTROL, "private, max-age=3600")
                .body(settings.getBackgroundImage());
    }

    @DeleteMapping("/background-image")
    @Operation(summary = "Remove the current background image", description = "Reverts to no background image; the solid-color selection is left null (no history is kept).")
    @ApiResponse(responseCode = "200", description = "Background image removed successfully")
    public BoardSettingsResponseDTO deleteImage() {
        return boardSettingsMapper.toResponseDTO(boardSettingsService.removeImage());
    }
}
