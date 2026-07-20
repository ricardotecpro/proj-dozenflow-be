package com.dozenflow.be.boardsettings;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Set;

@Service
public class BoardSettingsService {

    static final long MAX_IMAGE_SIZE_BYTES = 5L * 1024 * 1024;

    // Image-only allowlist, deliberately duplicated from AttachmentService's
    // (which also allows PDFs/docs) — only 2 call sites, different
    // allowlists, not worth a shared utility for a 3-line check.
    private static final Set<String> ALLOWED_IMAGE_CONTENT_TYPES = Set.of(
            "image/png", "image/jpeg", "image/gif", "image/webp"
    );

    private static final long SETTINGS_ID = 1L;

    private final BoardSettingsRepository boardSettingsRepository;

    public BoardSettingsService(BoardSettingsRepository boardSettingsRepository) {
        this.boardSettingsRepository = boardSettingsRepository;
    }

    public BoardSettings get() {
        return getOrCreate();
    }

    @Transactional
    public BoardSettings updateColor(String backgroundColorId) {
        BoardSettings settings = getOrCreate();
        settings.setBackgroundColorId(backgroundColorId);
        settings.setBackgroundImage(null);
        settings.setBackgroundImageContentType(null);
        return boardSettingsRepository.save(settings);
    }

    @Transactional
    public BoardSettings uploadImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidBackgroundImageException("File is empty");
        }
        if (file.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new InvalidBackgroundImageException("File exceeds the 5MB size limit");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_CONTENT_TYPES.contains(contentType)) {
            throw new InvalidBackgroundImageException("Unsupported file type: " + contentType);
        }

        BoardSettings settings = getOrCreate();
        try {
            settings.setBackgroundImage(file.getBytes());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read uploaded file", e);
        }
        settings.setBackgroundImageContentType(contentType);
        settings.setBackgroundColorId(null);
        return boardSettingsRepository.save(settings);
    }

    @Transactional
    public BoardSettings removeImage() {
        BoardSettings settings = getOrCreate();
        settings.setBackgroundImage(null);
        settings.setBackgroundImageContentType(null);
        return boardSettingsRepository.save(settings);
    }

    public BoardSettings getImage() {
        BoardSettings settings = getOrCreate();
        if (settings.getBackgroundImage() == null) {
            throw new EntityNotFoundException("No background image is currently set");
        }
        return settings;
    }

    private BoardSettings getOrCreate() {
        return boardSettingsRepository.findById(SETTINGS_ID).orElseGet(() -> {
            BoardSettings settings = new BoardSettings();
            settings.setId(SETTINGS_ID);
            return settings;
        });
    }
}
