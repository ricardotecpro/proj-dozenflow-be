package com.dozenflow.be.attachment;

import com.dozenflow.be.task.Task;
import com.dozenflow.be.task.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Set;

@Service
public class AttachmentService {

    static final long MAX_SIZE_BYTES = 5L * 1024 * 1024;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/png", "image/jpeg", "image/gif", "image/webp",
            "application/pdf",
            "text/plain",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    private final AttachmentRepository attachmentRepository;
    private final TaskRepository taskRepository;

    public AttachmentService(AttachmentRepository attachmentRepository, TaskRepository taskRepository) {
        this.attachmentRepository = attachmentRepository;
        this.taskRepository = taskRepository;
    }

    public List<AttachmentSummary> findAllByTaskId(Long taskId) {
        return attachmentRepository.findSummariesByTaskId(taskId);
    }

    public Attachment upload(Long taskId, MultipartFile file) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + taskId));

        if (file.isEmpty()) {
            throw new InvalidAttachmentException("File is empty");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new InvalidAttachmentException("File exceeds the 5MB size limit");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new InvalidAttachmentException("Unsupported file type: " + contentType);
        }

        Attachment attachment = new Attachment();
        attachment.setTask(task);
        attachment.setFileName(file.getOriginalFilename());
        attachment.setContentType(contentType);
        attachment.setSizeBytes(file.getSize());
        try {
            attachment.setData(file.getBytes());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read uploaded file", e);
        }

        return attachmentRepository.save(attachment);
    }

    public Attachment getForDownload(Long taskId, Long attachmentId) {
        return findOwnedAttachment(taskId, attachmentId);
    }

    public void delete(Long taskId, Long attachmentId) {
        Attachment attachment = findOwnedAttachment(taskId, attachmentId);
        attachmentRepository.delete(attachment);
    }

    private Attachment findOwnedAttachment(Long taskId, Long attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new EntityNotFoundException("Attachment not found with id: " + attachmentId));
        if (!attachment.getTask().getId().equals(taskId)) {
            throw new EntityNotFoundException("Attachment not found with id: " + attachmentId);
        }
        return attachment;
    }
}
