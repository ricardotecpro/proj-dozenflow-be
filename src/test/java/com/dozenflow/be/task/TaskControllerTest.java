package com.dozenflow.be.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void getAllTasks_returnsEmptyList_whenNoTasksExist() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void createTask_persistsAndReturnsCreatedTask() throws Exception {
        String payload = """
                {"title":"Write tests","description":"Cover the API","status":"A_FAZER","taskOrder":1}
                """;

        mockMvc.perform(post("/api/tasks")
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value("Write tests"))
                .andExpect(jsonPath("$.status").value("A_FAZER"));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void createTask_returnsBadRequest_whenTitleIsBlank() throws Exception {
        String payload = """
                {"title":"","description":"No title","status":"A_FAZER","taskOrder":1}
                """;

        mockMvc.perform(post("/api/tasks")
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void updateTask_updatesExistingTask() throws Exception {
        Task existing = new Task();
        existing.setTitle("Original");
        existing.setStatus(TaskStatus.A_FAZER);
        existing.setTaskOrder(0);
        existing = taskRepository.save(existing);

        String payload = """
                {"title":"Updated","description":"Now in progress","status":"EM_ANDAMENTO","taskOrder":2}
                """;

        mockMvc.perform(put("/api/tasks/{id}", existing.getId())
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"))
                .andExpect(jsonPath("$.status").value("EM_ANDAMENTO"));
    }

    @Test
    void updateTask_returnsNotFound_whenTaskDoesNotExist() throws Exception {
        String payload = """
                {"title":"Updated","description":"Now in progress","status":"EM_ANDAMENTO","taskOrder":2}
                """;

        mockMvc.perform(put("/api/tasks/{id}", 999_999L)
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTask_removesExistingTask() throws Exception {
        Task existing = new Task();
        existing.setTitle("To be deleted");
        existing.setStatus(TaskStatus.A_FAZER);
        existing.setTaskOrder(0);
        existing = taskRepository.save(existing);

        mockMvc.perform(delete("/api/tasks/{id}", existing.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/tasks"))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void deleteTask_returnsNotFound_whenTaskDoesNotExist() throws Exception {
        mockMvc.perform(delete("/api/tasks/{id}", 999_999L))
                .andExpect(status().isNotFound());
    }
}
