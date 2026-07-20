package com.dozenflow.be.attachment;

import com.dozenflow.be.task.Task;
import com.dozenflow.be.task.TaskRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@TestPropertySource(properties = "rate-limit.max-requests-per-window=100000")
@AutoConfigureMockMvc
@Transactional
class AttachmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private AttachmentRepository attachmentRepository;

    private Task createTask() {
        Task task = new Task();
        task.setTitle("Task with attachments");
        task.setListId(1L);
        return taskRepository.save(task);
    }

    @Test
    void getAllForTask_returnsEmptyList_whenNoAttachmentsExist() throws Exception {
        Task task = createTask();

        mockMvc.perform(get("/api/tasks/{taskId}/attachments", task.getId()))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void upload_persistsAndReturnsMetadata_withoutTheFileBytes() throws Exception {
        Task task = createTask();
        MockMultipartFile file = new MockMultipartFile("file", "mockup.png", "image/png", "fake-bytes".getBytes());

        mockMvc.perform(multipart("/api/tasks/{taskId}/attachments", task.getId()).file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.fileName").value("mockup.png"))
                .andExpect(jsonPath("$.contentType").value("image/png"))
                .andExpect(jsonPath("$.sizeBytes").value(file.getSize()))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void upload_returnsNotFound_whenTaskDoesNotExist() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "mockup.png", "image/png", "fake-bytes".getBytes());

        mockMvc.perform(multipart("/api/tasks/{taskId}/attachments", 999_999L).file(file))
                .andExpect(status().isNotFound());
    }

    @Test
    void upload_returnsBadRequest_whenContentTypeIsNotAllowlisted() throws Exception {
        Task task = createTask();
        MockMultipartFile file = new MockMultipartFile(
                "file", "script.exe", "application/x-msdownload", "fake-bytes".getBytes());

        mockMvc.perform(multipart("/api/tasks/{taskId}/attachments", task.getId()).file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    void upload_returnsBadRequest_whenFileExceedsSizeLimit() throws Exception {
        Task task = createTask();
        byte[] tooLarge = new byte[(int) AttachmentService.MAX_SIZE_BYTES + 1];
        MockMultipartFile file = new MockMultipartFile("file", "big.png", "image/png", tooLarge);

        mockMvc.perform(multipart("/api/tasks/{taskId}/attachments", task.getId()).file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    void download_returnsFileBytesWithCorrectHeaders() throws Exception {
        Task task = createTask();
        Attachment attachment = new Attachment();
        attachment.setTask(task);
        attachment.setFileName("mockup.png");
        attachment.setContentType("image/png");
        attachment.setSizeBytes(10);
        attachment.setData("fake-bytes".getBytes());
        attachment = attachmentRepository.save(attachment);

        mockMvc.perform(get("/api/tasks/{taskId}/attachments/{attachmentId}/download", task.getId(), attachment.getId()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/png"))
                // Spring's ContentDisposition.filename(name, charset) always emits both the
                // ASCII-safe fallback (MIME encoded-word) and the RFC 5987 extended form,
                // even for a plain ASCII name — that's correct, properly-encoded output.
                .andExpect(header().string(
                        "Content-Disposition",
                        "attachment; filename=\"=?UTF-8?Q?mockup.png?=\"; filename*=UTF-8''mockup.png"))
                .andExpect(content().bytes("fake-bytes".getBytes()));
    }

    @Test
    void download_returnsNotFound_whenAttachmentDoesNotExist() throws Exception {
        Task task = createTask();

        mockMvc.perform(get("/api/tasks/{taskId}/attachments/{attachmentId}/download", task.getId(), 999_999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void view_returnsFileBytesInline() throws Exception {
        Task task = createTask();
        Attachment attachment = new Attachment();
        attachment.setTask(task);
        attachment.setFileName("mockup.png");
        attachment.setContentType("image/png");
        attachment.setSizeBytes(10);
        attachment.setData("fake-bytes".getBytes());
        attachment = attachmentRepository.save(attachment);

        mockMvc.perform(get("/api/tasks/{taskId}/attachments/{attachmentId}/view", task.getId(), attachment.getId()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/png"))
                .andExpect(header().string(
                        "Content-Disposition",
                        "inline; filename=\"=?UTF-8?Q?mockup.png?=\"; filename*=UTF-8''mockup.png"))
                .andExpect(content().bytes("fake-bytes".getBytes()));
    }

    @Test
    void view_returnsNotFound_whenAttachmentDoesNotExist() throws Exception {
        Task task = createTask();

        mockMvc.perform(get("/api/tasks/{taskId}/attachments/{attachmentId}/view", task.getId(), 999_999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void view_returnsNotFound_whenAttachmentBelongsToAnotherTask() throws Exception {
        Task task = createTask();
        Task otherTask = createTask();
        Attachment attachment = new Attachment();
        attachment.setTask(otherTask);
        attachment.setFileName("mockup.png");
        attachment.setContentType("image/png");
        attachment.setSizeBytes(10);
        attachment.setData("fake-bytes".getBytes());
        attachment = attachmentRepository.save(attachment);

        mockMvc.perform(get("/api/tasks/{taskId}/attachments/{attachmentId}/view", task.getId(), attachment.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_removesExistingAttachment() throws Exception {
        Task task = createTask();
        Attachment attachment = new Attachment();
        attachment.setTask(task);
        attachment.setFileName("mockup.png");
        attachment.setContentType("image/png");
        attachment.setSizeBytes(10);
        attachment.setData("fake-bytes".getBytes());
        attachment = attachmentRepository.save(attachment);

        mockMvc.perform(delete("/api/tasks/{taskId}/attachments/{attachmentId}", task.getId(), attachment.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/tasks/{taskId}/attachments", task.getId()))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void delete_returnsNotFound_whenAttachmentDoesNotExist() throws Exception {
        Task task = createTask();

        mockMvc.perform(delete("/api/tasks/{taskId}/attachments/{attachmentId}", task.getId(), 999_999L))
                .andExpect(status().isNotFound());
    }
}
