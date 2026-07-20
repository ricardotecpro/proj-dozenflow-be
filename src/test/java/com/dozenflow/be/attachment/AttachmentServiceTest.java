package com.dozenflow.be.attachment;

import com.dozenflow.be.task.Task;
import com.dozenflow.be.task.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceTest {

    @Mock
    private AttachmentRepository attachmentRepository;

    @Mock
    private TaskRepository taskRepository;

    private AttachmentService attachmentService;

    @BeforeEach
    void setUp() {
        attachmentService = new AttachmentService(attachmentRepository, taskRepository);
    }

    @Test
    void upload_savesAttachment_whenFileIsValid() {
        Task task = new Task();
        task.setId(1L);
        MockMultipartFile file = new MockMultipartFile("file", "mockup.png", "image/png", "fake-bytes".getBytes());

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(attachmentRepository.save(any(Attachment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Attachment result = attachmentService.upload(1L, file);

        assertThat(result.getTask()).isEqualTo(task);
        assertThat(result.getFileName()).isEqualTo("mockup.png");
        assertThat(result.getContentType()).isEqualTo("image/png");
        assertThat(result.getSizeBytes()).isEqualTo(file.getSize());
        assertThat(result.getData()).isEqualTo("fake-bytes".getBytes());
    }

    @Test
    void upload_throwsEntityNotFoundException_whenTaskDoesNotExist() {
        MockMultipartFile file = new MockMultipartFile("file", "mockup.png", "image/png", "fake-bytes".getBytes());
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attachmentService.upload(99L, file))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void upload_throwsInvalidAttachmentException_whenFileIsEmpty() {
        Task task = new Task();
        task.setId(1L);
        MockMultipartFile file = new MockMultipartFile("file", "empty.png", "image/png", new byte[0]);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> attachmentService.upload(1L, file))
                .isInstanceOf(InvalidAttachmentException.class);

        verify(attachmentRepository, never()).save(any());
    }

    @Test
    void upload_throwsInvalidAttachmentException_whenFileExceedsSizeLimit() {
        Task task = new Task();
        task.setId(1L);
        byte[] tooLarge = new byte[(int) AttachmentService.MAX_SIZE_BYTES + 1];
        MockMultipartFile file = new MockMultipartFile("file", "big.png", "image/png", tooLarge);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> attachmentService.upload(1L, file))
                .isInstanceOf(InvalidAttachmentException.class)
                .hasMessageContaining("5MB");

        verify(attachmentRepository, never()).save(any());
    }

    @Test
    void upload_throwsInvalidAttachmentException_whenContentTypeIsNotAllowlisted() {
        Task task = new Task();
        task.setId(1L);
        MockMultipartFile file = new MockMultipartFile(
                "file", "script.exe", "application/x-msdownload", "fake-bytes".getBytes());
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> attachmentService.upload(1L, file))
                .isInstanceOf(InvalidAttachmentException.class)
                .hasMessageContaining("Unsupported");

        verify(attachmentRepository, never()).save(any());
    }

    @Test
    void getForDownload_returnsAttachment_whenItBelongsToTask() {
        Task task = new Task();
        task.setId(1L);
        Attachment attachment = new Attachment();
        attachment.setId(2L);
        attachment.setTask(task);

        when(attachmentRepository.findById(2L)).thenReturn(Optional.of(attachment));

        Attachment result = attachmentService.getForDownload(1L, 2L);

        assertThat(result).isEqualTo(attachment);
    }

    @Test
    void getForDownload_throwsEntityNotFoundException_whenAttachmentBelongsToDifferentTask() {
        Task task = new Task();
        task.setId(1L);
        Attachment attachment = new Attachment();
        attachment.setId(2L);
        attachment.setTask(task);

        when(attachmentRepository.findById(2L)).thenReturn(Optional.of(attachment));

        assertThatThrownBy(() -> attachmentService.getForDownload(999L, 2L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void delete_removesAttachment_whenItBelongsToTask() {
        Task task = new Task();
        task.setId(1L);
        Attachment attachment = new Attachment();
        attachment.setId(2L);
        attachment.setTask(task);

        when(attachmentRepository.findById(2L)).thenReturn(Optional.of(attachment));

        attachmentService.delete(1L, 2L);

        verify(attachmentRepository).delete(attachment);
    }
}
