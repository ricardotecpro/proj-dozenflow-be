package com.dozenflow.be.checklist;

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
class ChecklistItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ChecklistItemRepository checklistItemRepository;

    private Task createTask() {
        Task task = new Task();
        task.setTitle("Task with checklist");
        task.setListId(1L);
        return taskRepository.save(task);
    }

    @Test
    void getAllForTask_returnsEmptyList_whenNoItemsExist() throws Exception {
        Task task = createTask();

        mockMvc.perform(get("/api/tasks/{taskId}/checklist-items", task.getId()))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void create_persistsAndReturnsCreatedItem() throws Exception {
        Task task = createTask();
        String payload = """
                {"title":"Escrever testes","done":false,"itemOrder":0}
                """;

        mockMvc.perform(post("/api/tasks/{taskId}/checklist-items", task.getId())
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value("Escrever testes"))
                .andExpect(jsonPath("$.done").value(false));
    }

    @Test
    void create_returnsNotFound_whenTaskDoesNotExist() throws Exception {
        String payload = """
                {"title":"Escrever testes","done":false,"itemOrder":0}
                """;

        mockMvc.perform(post("/api/tasks/{taskId}/checklist-items", 999_999L)
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_returnsBadRequest_whenTitleIsBlank() throws Exception {
        Task task = createTask();
        String payload = """
                {"title":"","done":false,"itemOrder":0}
                """;

        mockMvc.perform(post("/api/tasks/{taskId}/checklist-items", task.getId())
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_updatesExistingItem() throws Exception {
        Task task = createTask();
        ChecklistItem item = new ChecklistItem();
        item.setTask(task);
        item.setTitle("Old");
        item.setDone(false);
        item = checklistItemRepository.save(item);

        String payload = """
                {"title":"New","done":true,"itemOrder":1}
                """;

        mockMvc.perform(put("/api/tasks/{taskId}/checklist-items/{itemId}", task.getId(), item.getId())
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New"))
                .andExpect(jsonPath("$.done").value(true));
    }

    @Test
    void update_returnsNotFound_whenItemBelongsToAnotherTask() throws Exception {
        Task task = createTask();
        Task otherTask = createTask();
        ChecklistItem item = new ChecklistItem();
        item.setTask(otherTask);
        item.setTitle("Item");
        item = checklistItemRepository.save(item);

        String payload = """
                {"title":"New","done":true,"itemOrder":1}
                """;

        mockMvc.perform(put("/api/tasks/{taskId}/checklist-items/{itemId}", task.getId(), item.getId())
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_removesExistingItem() throws Exception {
        Task task = createTask();
        ChecklistItem item = new ChecklistItem();
        item.setTask(task);
        item.setTitle("Item");
        item = checklistItemRepository.save(item);

        mockMvc.perform(delete("/api/tasks/{taskId}/checklist-items/{itemId}", task.getId(), item.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/tasks/{taskId}/checklist-items", task.getId()))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void delete_returnsNotFound_whenItemDoesNotExist() throws Exception {
        Task task = createTask();

        mockMvc.perform(delete("/api/tasks/{taskId}/checklist-items/{itemId}", task.getId(), 999_999L))
                .andExpect(status().isNotFound());
    }
}
