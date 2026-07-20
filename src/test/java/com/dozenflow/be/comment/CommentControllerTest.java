package com.dozenflow.be.comment;

import com.dozenflow.be.task.Task;
import com.dozenflow.be.task.TaskRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@TestPropertySource(properties = "rate-limit.max-requests-per-window=100000")
@AutoConfigureMockMvc
@Transactional
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CommentRepository commentRepository;

    private Task createTask() {
        Task task = new Task();
        task.setTitle("Task with comments");
        task.setListId(1L);
        return taskRepository.save(task);
    }

    @Test
    void getAllForTask_returnsEmptyList_whenNoCommentsExist() throws Exception {
        Task task = createTask();

        mockMvc.perform(get("/api/tasks/{taskId}/comments", task.getId()))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void create_persistsAndReturnsCreatedComment() throws Exception {
        Task task = createTask();
        String payload = """
                {"body":"Já revisei, pode seguir."}
                """;

        mockMvc.perform(post("/api/tasks/{taskId}/comments", task.getId())
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.body").value("Já revisei, pode seguir."))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    void create_returnsNotFound_whenTaskDoesNotExist() throws Exception {
        String payload = """
                {"body":"Comentário"}
                """;

        mockMvc.perform(post("/api/tasks/{taskId}/comments", 999_999L)
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_returnsBadRequest_whenBodyIsBlank() throws Exception {
        Task task = createTask();
        String payload = """
                {"body":""}
                """;

        mockMvc.perform(post("/api/tasks/{taskId}/comments", task.getId())
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_removesExistingComment() throws Exception {
        Task task = createTask();
        Comment comment = new Comment();
        comment.setTask(task);
        comment.setBody("Comentário");
        comment = commentRepository.save(comment);

        mockMvc.perform(delete("/api/tasks/{taskId}/comments/{commentId}", task.getId(), comment.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/tasks/{taskId}/comments", task.getId()))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void delete_returnsNotFound_whenCommentDoesNotExist() throws Exception {
        Task task = createTask();

        mockMvc.perform(delete("/api/tasks/{taskId}/comments/{commentId}", task.getId(), 999_999L))
                .andExpect(status().isNotFound());
    }
}
